# Btle-Scanner
Library for scan the bluetooth low energy device like beacons, healthcare and fitness devices

When i start wrote this library my task was to scan the beacons. 
After some time i have realized that this library can scan more than the simple beacon
so i made some code changes. Now you can define what type of devices you want to find and how.

## Contents

- [Installation](#installation)
- [How to use](#how-to-use)
- [Bugs and feedback](#bugs-and-feedback)
- [Credits](#credits)
- [License](#license)

## Installation

[ ![Download](https://api.bintray.com/packages/maro/maven/BtleScanner/images/download.svg) ](https://bintray.com/maro/maven/BtleScanner/_latestVersion)

    compile 'com.marozzi:btlescanner:1.0.0'

## How to use

### Define a ScanServiceCallback

The main callback 

    private ScanServiceCallback scanServiceCallback = new ScanServiceCallback() {
        @Override
        public void onBeaconFound(@NonNull iBeacon beacon) {

        }

        @Override
        public void onError(@NonNull ScanError scanError) {

        }
    };

### Define a provider

A provider receive the devices found by the Scan Service and if the devices matches can use the callback to notify the user

    public class EquipmentProvider implements Provider<iBeacon> {

        private ProviderCallback<iBeacon> callback;

        @Override
        public void start() {
            //called when the service start scanning for the devices
        }

        @Override
        public void stop() {
             //called when the service stop scanning for the devices, we can release some unnecessary resources
        }

        @Override
        public void setProviderCallback(@NonNull ProviderCallback<iBeacon> callback) {
            this.callback = callback;
        }

        @Override
        public void elaborateBeacon(@NonNull iBeacon beacon) {
            //when the scanner found a beacon will call this method, if the beacon is ok we can use the ProviderCallback to notify the user
        }
    }

### Use the ScanService

    private ScanService scanService;

    public void init(Context context) {
        scanService = new DefaultScanService(context, new DefaultBluetoothFactory(), new EquipmentProvider());
        scanService.setCallback(scanServiceCallback);
    }

    public void start() {
        scanService.start();
    }

    public void stop() {
        scanService.stop();
    }

## Bugs and Feedback

For bugs, feature requests, and discussion please use [GitHub Issues](https://github.com/JMaroz/BtleScanner/issues)

## Credits

This library was inspired by some other repos and some bluetooth articles found online

## License

    MIT License

    Copyright (c) 2017 JMaroz

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.