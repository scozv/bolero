#!/usr/bin/env bash
# activator run -Dconfig.resource=release.conf

BOLERO_GIT_SHA1=`git rev-parse --short HEAD`
BOLERO_GIT_BRANCH=`git rev-parse --abbrev-ref HEAD | sed 's/feature\///g'`
BOLERO_DPKG_SERVERNAME="bolero-server"
BOLERO_DPKG_VERSION="1.0-SNAPSHOT"
BOLERO_DPKG_FULLNAME="bolero-server_1.0-SNAPSHOT"

BOLERO_ENV='Prod'
BOLERO_HTTP_PORT="9000"
echo '准备发布bolero-server，默认为生产发布'
echo '即，appkey和secret都使用真实编号'
echo '请问是发布为测试环境还是生产环境 ?'
echo '测试发布请键入Test，否则默认为生产：'
read BOLERO_ENV

echo '修改配置文件'
change_conf() {
  # 修复之前的任何错误更改
  git checkout build.sbt

  if [ 'Test' == "$BOLERO_ENV" ]; then
    echo '测试环境需要修改配置文件'
    BOLERO_DPKG_SERVERNAME="bolero-server-$BOLERO_GIT_BRANCH"
    echo "将Git分支添加到Server Name: $BOLERO_DPKG_SERVERNAME"
    sed -i "s/bolero-server/$BOLERO_DPKG_SERVERNAME/" build.sbt

    BOLERO_DPKG_VERSION="1.0-SNAPSHOT-$BOLERO_GIT_SHA1"
    echo "将Git sha1添加到文件名：$BOLERO_DPKG_VERSION"
    sed -i "s/1.0-SNAPSHOT/$BOLERO_DPKG_VERSION/" build.sbt

    BOLERO_DPKG_FULLNAME="${BOLERO_DPKG_SERVERNAME}_$BOLERO_DPKG_VERSION"
    echo "完整的文件名为：$BOLERO_DPKG_FULLNAME"

    conf_str_prod='"\-Dconfig.file=\/usr\/share\/\${packageName.value}\/conf\/release.conf"'
    conf_str_eval='"\-Dconfig.file=\/usr\/share\/\${packageName.value}\/conf\/application.conf"'
    sed -i "s/$conf_str_prod/$conf_str_eval/g" build.sbt

    echo '请输入四位测试发布的HTTP Port，默认为9000：'
    read BOLERO_HTTP_PORT

    if [[ "$BOLERO_HTTP_PORT" =~ [0-9][0-9][0-9][0-9] ]]; then
      BOLERO_HTTP_PORT="$BOLERO_HTTP_PORT"
    else
      BOLERO_HTTP_PORT="9000"
    fi
    echo "使用配置：-Dhttp.port=$BOLERO_HTTP_PORT"
    sed -i "s/-Dhttp.port=9000/-Dhttp.port=$BOLERO_HTTP_PORT/g" build.sbt

  else
    BOLERO_DPKG_FULLNAME="bolero-server_1.0-SNAPSHOT"
  fi

  # activator test
  activator playUpdateSecret
  echo "完整的配置清单"
  cat build.sbt
  sbt debian:packageBin
  # activator run

  # 修复之前的任何错误更改
  git checkout build.sbt
  git checkout conf/application.conf
}

echo '生成Native Package'
change_conf

# http://askubuntu.com/a/615086

# https://wiki.ubuntu.com/SystemdForUpstartUsers
# Permanent switch back to upstart
# Install the upstart-sysv package, which will remove ubuntu-standard and systemd-sysv
# (but should not remove anything else -- if it does, yell!), and run sudo update-initramfs -u.
# After that, grub's "Advanced options" menu will have a corresponding "Ubuntu, with Linux ... (systemd)"
# entry where you can do an one-time boot with systemd.
#
# If you want to switch back to systemd, install the systemd-sysv and ubuntu-standard packages.

# Ubuntu 15.04+ run only once
# sudo apt-get -y install upstart-sysv
# sudo update-initramfs -u

# Ubuntu 14.04 run only once
# sudo apt-get -y install fakeroot
echo '安装并启动'
echo "当前为 $BOLERO_ENV 环境"
echo "安装包 ${BOLERO_DPKG_FULLNAME}_all"
BOLERO_INST_SH="sudo dpkg -i target/${BOLERO_DPKG_FULLNAME}_all.deb"
echo "请执行 $BOLERO_INST_SH"

# stop | start
# sudo start bolero-server
# sudo stop  bolero-server

unset BOLERO_GIT_SHA1
unset BOLERO_GIT_BRANCH
unset BOLERO_DPKG_SERVERNAME
unset BOLERO_DPKG_VERSION
unset BOLERO_DPKG_FULLNAME
unset BOLERO_ENV
unset BOLERO_HTTP_PORT