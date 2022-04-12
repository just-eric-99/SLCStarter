# SLCStarter

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
|	|	|	|	├── TouchDisplayAdminLogin.fxml
|	|	|	|	├── TouchDisplayConfirmation.fxml
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

