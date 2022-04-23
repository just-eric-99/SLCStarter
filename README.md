# README

**SLCStarter** is a Smart Locker Controller which can interact with an Octopus card reader, barcode reader, lockers, touch screen display and smart locker server.

## Directories Structure

```markdown
SLCStarter
├── etc
|	├── SLC.cfg
|	├── SLC.SLCEmulatorStarter.log
|	├── SLSvr.cfg
|	└── SLSvr.SLSvrEmulatorStarter.log
├── src
|	├── AppKickstarter
|	|	├── AppKickstarter.java
|	|	├── misc
|	|	|	├── AppThread.java
|	|	|	├── Lib.java
|	|	|	├── LogFormatter.java
|	|	|	├── Mbox.java
|	|	|	└── Msg.java
|	|	└── timer
|	|		└── Timer.java
|	├── Common
|	|	├── SimpleTimer
|	|	|	├── PeriodAction.java
|	|	|	├── SimpleTimer.java
|	|	|	├── WakeUpAction.java
|	|	|	└── WakeUpCondition.java
|	|	└── LockerSize.java
|	|	└── DiagnosticStatus.java
|	├── SLC
|	|	├── BarcodeReaderDriver
|	|	|	├── Emulator
|	|	|	|	├── BarcodeReaderEmulator.java
|	|	|	|	├── BarcodeReaderEmulator.fxml
|	|	|	|	└── BarcodeReaderEmulatorController.java
|	|	|	└── BarcodeReaderDriver.java
|	|	├── HWHandler
|	|	|	└── HWHandler.java
|	|	├── Locker
|	|	|	├── Emulator
|	|	|	|	├── LockerEmulator.java
|	|	|	|	├── LockerEmulator.fxml
|	|	|	|	└── LockerEmulatorController.java
|	|	|	├── Locker.java
|	|	|	└── LockerDriver.java
|	|	├── OctopusCardReaderDriver
|	|	|	├── Emulator
|	|	|	|	├── OctopusCardReaderEmulator.java
|	|	|	|	├── OctopusCardReaderEmulator.fxml
|	|	|	|	└── OctopusCardReaderEmulatorController.java
|	|	|	└── OctopusCardReaderDriver.java
|	|	├── SLC
|	|	|	├── HWStatus.java
|	|	|	├── Screen.java
|	|	|	├── SLC.java
|	|	|	└── SmallLocker.java
|	|	├── SLSvrHandler
|	|	|	└── SLSvrHandler.java
|	|	├── TouchDisplayHandler
|	|	|	├── Emulator
|	|	|	|	├── TouchDisplayEmulator.java
|	|	|	|	├── TouchDisplayEmulator.fxml
|	|	|	|	├── TouchDisplayEmulatorController.java
|	|	|	|	├── TouchDisplayEnterPasscode.fxml
|	|	|	|	├── TouchDisplayLockerNotClose.fxml
|	|	|	|	├── TouchDisplayMainMenu.fxml
|	|	|	|	├── TouchDisplayPayment.fxml
|	|	|	|	├── TouchDisplayPaymentFailed.fxml
|	|	|	|	├── TouchDisplayPaymentSucceeded.fxml
|	|	|	|	├── TouchDisplayScanBarcode.fxml
|	|	|	|	├── TouchDisplayServerDown.fxml
|	|	|	|	└── TouchDisplayShowLocker.fxml
|	|	|	└── TouchDisplayHandler.java
|	|	├── SLCEmulatorStarter.java
|	|	└── SLCStarter.java
|	└── SLSvr
|		├── Emulator
|		|	├── SLSvrEmulator.java
|		|	├── SLSvrEmulator.fxml
|		|	└── SLSvrEmulatorController.java
|		├── SLSvr
|		|	├── Locker.java
|		|	├── Package.java
|		|	├── Payment.java
|		|	├── SendPackageListener.java
|		|	└── SLSvr.java
|		├── SLSvrEmulatorStarter.java
|		└── SLSvrStarter.java
└── README.md
```

## Compiling the program

Before starting the program, we made use of Json data format, therefore you would need to download and implement the Json jar into the project in order to run it (This is a general guide on how to implement the jar into your project, steps might be different depending on the IDE you are using):

1. Download the json-20220320.jar through this link: https://repo1.maven.org/maven2/org/json/json/20220320/json-20220320.jar
2. Go into Project Structure, click into Modules under Project Settings
3. Add the jar

### To start the program:

1. Start SLSvrEmulator.java in the src.SLSvr package
2. Start SLCEmulatorStarter.java in the the src.SLC package

### To stop the program:

1. Close Touch Display by clicking the cross on the top-right hand side of the GUI
2. Close Smart Locker Server by clicking the cross on the top-right hand side of the GUI

### To reset the program:

1. Delete HKGLK01.db file in the main directory of the project (if any).
2. Delete server_package.db file in the main directory of the project (if any).
3. Start SLSvrEmulatorStarter.java in the src.SLSvr package.
4. Start SLCEmulatorStarter.java in the src.SLC package.