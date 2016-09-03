#!/usr/bin/env bash
# activator run -Dconfig.resource=release.conf

BOLERO_GIT_SHA1=`git rev-parse --short HEAD`
BOLERO_GIT_BRANCH=`git rev-parse --abbrev-ref HEAD | sed 's/feature\///g'`
BOLERO_DPKG_SERVERNAME="bolero-server"
BOLERO_DPKG_VERSION="1.0-SNAPSHOT"
BOLERO_DPKG_FULLNAME="bolero-server_1.0-SNAPSHOT"

BOLERO_ENV='Prod'
BOLERO_HTTP_PORT="9000"
echo 'Ready for deployment, Release Env as default'
echo '  appkey and secret are all in Release Env'
echo 'Confirm the Env Release or Test ?'
echo '  Type Test, otherwise will be in Release Mode:'
read BOLERO_ENV

echo 'Switch to Release Conf'
change_conf() {
  # 修复之前的任何错误更改
  git checkout build.sbt

  if [ 'Test' == "$BOLERO_ENV" ]; then
    echo 'Test Conf'
    BOLERO_DPKG_SERVERNAME="bolero-server-$BOLERO_GIT_BRANCH"
    echo "Append git branch to service name: $BOLERO_DPKG_SERVERNAME"
    sed -i "s/bolero-server/$BOLERO_DPKG_SERVERNAME/" build.sbt

    BOLERO_DPKG_VERSION="1.0-SNAPSHOT-$BOLERO_GIT_SHA1"
    echo "Append git sha1 to service name: $BOLERO_DPKG_VERSION"
    sed -i "s/1.0-SNAPSHOT/$BOLERO_DPKG_VERSION/" build.sbt

    BOLERO_DPKG_FULLNAME="${BOLERO_DPKG_SERVERNAME}_$BOLERO_DPKG_VERSION"
    echo "Final service name is: $BOLERO_DPKG_FULLNAME"

    conf_str_prod='"\-Dconfig.file=\/usr\/share\/\${packageName.value}\/conf\/release.conf"'
    conf_str_eval='"\-Dconfig.file=\/usr\/share\/\${packageName.value}\/conf\/application.conf"'
    sed -i "s/$conf_str_prod/$conf_str_eval/g" build.sbt

    echo 'Type the Test Mode HTTP Port, 9000 as default: '
    read BOLERO_HTTP_PORT

    if [[ "$BOLERO_HTTP_PORT" =~ [0-9][0-9][0-9][0-9] ]]; then
      BOLERO_HTTP_PORT="$BOLERO_HTTP_PORT"
    else
      BOLERO_HTTP_PORT="9000"
    fi
    echo "Apply the conf: -Dhttp.port=$BOLERO_HTTP_PORT"
    sed -i "s/-Dhttp.port=9000git s/-Dhttp.port=$BOLERO_HTTP_PORT/g" build.sbt

  else
    BOLERO_DPKG_FULLNAME="bolero-server_1.0-SNAPSHOT"
  fi

  # activator test
  activator playUpdateSecret
  echo "Review the conf"
  cat build.sbt
  sbt debian:packageBin
  # activator run

  # 修复之前的任何错误更改
  git checkout build.sbt
  git checkout conf/application.conf
}

echo 'Compile to Native Package'
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
echo 'Install the deb and start service'
echo "Currently, we are in $BOLERO_ENV Mode"
echo "Package name: ${BOLERO_DPKG_FULLNAME}_all"
BOLERO_INST_SH="sudo dpkg -i target/${BOLERO_DPKG_FULLNAME}_all.deb"
echo "Please execute $BOLERO_INST_SH"

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