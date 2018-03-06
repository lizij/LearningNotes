# Ubuntu Settings
This is about something after Ubuntu 16.04 initial installation

## Basic enviroment
Change language to english: `Settings` -> `Language Support`
Change apt sources: `Settings` -> `Software&Updates`

## zsh&oh-my-zsh

```shell
sudo apt install zsh curl git vim
sh -c "$(curl -fsSL https://raw.github.com/robbyrussell/oh-my-zsh/master/tools/install.sh)"
```

## Typora

[deb](https://typora.io/linux/Typora-linux-x64.tar.gz)

## unity-tweak-tool&macbuntu theme

```shell
sudo add-apt-repository ppa:noobslab/macbuntu
sudo apt update
sudo apt install unity-tweak-tool macbuntu-os-icons-lts-v7 macbuntu-os-ithemes-lts-v7
```

Choose macbuntu-os-x theme

##docker

```shell
wget -qO- http://get.docker.com| sh
sudo usermod -aG docker xxx
```

## Chrome

[deb](https://www.google.cn/chrome/browser/desktop/index.html)

## Sogou Input

[deb](https://pinyin.sogou.com/linux/download.php?f=linux&bit=64)

## Gparted

```shell
sudo apt install gparted
```

## Sublime

[deb](https://www.sublimetext.com/3dev)

```shell
# 3156
----- BEGIN LICENSE -----
eldon
Single User License
EA7E-1122628
C0360740 20724B8A 30420C09 6D7E046F
3F5D5FBB 17EF95DA 2BA7BB27 CCB14947
27A316BE 8BCF4BC0 252FB8FF FD97DF71
B11A1DA9 F7119CA0 31984BB9 7D71700C
2C728BF8 B952E5F5 B941FF64 6D7979DA
B8EB32F8 8D415F8E F16FE657 A35381CC
290E2905 96E81236 63D2B06D E5F01A69
84174B79 7C467714 641A9013 94CA7162
------ END LICENSE ------
```

## Pycharm

[deb](http://data.services.jetbrains.com/products/download?code=PCP&platform=linux)

Add support for Chinese input for charm.sh, also work in other JetBrain's products in Linux

```shell
#!/bin/sh
#
# ---------------------------------------------------------------------
# PyCharm startup script.
# ---------------------------------------------------------------------
#
export GTK_IM_MODULE=fcitx
export QT_IM_MODULE=fcitx
export XMODIFIERS=@im=fcitx

message()
...
```

