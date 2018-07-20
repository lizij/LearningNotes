# Install fingerprint on Ubuntu

This is for users who have a fingerprint sensor on Ubuntu pc

## Test sensor

Add ppa source and update

```shell
sudo add-apt-repository ppa:fingerprint/fprint
sudo apt update
```

Install fprint-demo to test the sensor

```shell
sudo apt install fprint-demo
```

Open fprint-demo. If your sensor on the pc is successfully detected, fprint-demo will seems like this:

![](https://www.mobibrw.com/wp-content/uploads/2014/06/fprint_project_demo.png)

Then fprint-demo can be removed. Close it and run

```shell
sudo apt remove --purge fprint-demo
```

## Fingerprint login

Install libpam-fprintd

```shell
sudo apt install libpam-fprintd
```

It might need to reboot once

Open **User Account** in **Settings**. Fingerprint Login will appear.