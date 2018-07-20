##  Install NVIDIA driver

1. Download the driver installer in [NVIDIA](https://www.geforce.cn/drivers)

2. Press ```ctrl``` + ```alt``` + ```F1``` to enter the console

3. Disable ***nouveau***

   Create a file named blacklist-nouveau.conf in /etc/modprobe.d/ and insert:

   ```shell
   blacklist nouveau
   options nouveau modeset=0
   ```

   Then run:

   ```shell
   sudo update-initramfs -u
   reboot
   ```

4. Enter the console again and stop X Server

   ```shell
   sudo service lightdm stop
   sudo init 3
   sudo rm -rf /tmp/.X*
   ```

5. Run script

   ```shell
   sudo ./NVIDIA-Linux-x86-64-xxx.run
   ```