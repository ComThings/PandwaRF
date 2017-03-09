function main() {

  var freq = 433920000;
  var mod = 0x30;
  var drate = 3200;

  gollumPrint("------ RX Data setup --------");
  gollumRfRxSetup(freq, mod, drate, 50);

  gollumPrint("------ Data listen --------");
  var arrayData = gollumRfRxListen(250);

  gollumPrint("------ Data result (array) --------");
  gollumPrint(arrayData);

  gollumPrint("------ Data result --------");
  for(i = 0 ; i < arrayData.length ; i++ ){
    gollumPrint(i + ": " + arrayData[i].toString(16));
  }

  gollumPrint("------ Stopping RX --------");
  gollumRfRxStop();

}

main();