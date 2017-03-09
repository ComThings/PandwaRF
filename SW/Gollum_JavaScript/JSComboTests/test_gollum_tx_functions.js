function main() {

  var string = "\x88\x8e\x88";
  var size = 20;
  var freq = 433920000;
  var mod = 0x30;
  var drate = 3200;

  gollumRfTxSetup(freq, mod, drate);

  gollumRfTxSend("" + string);
}

main();