function main() {

  gollumPrint("---------- Before writting registers --------- ");
  var before =
      gollumRfRegisterRead(0xDF01, 5); // we start to read registers a "0xDF01",
                                       // and we finally read 5 registers

  for (var z = 0; z < before.length; z++) {
    gollumPrint("Value read: " + before[z].toString(16));
  }

  var values = [ 0xAB, 0xEF, 0xFF ]; // values in hexa

  gollumRfRegisterWrite(values, 0xDF01); // we start to write a value at
                                         // register which has address "0xDF01",
                                         // We write in 3 registers

  gollumPrint("---------- After writting registers --------- ");
  var after =
      gollumRfRegisterRead(0xDF01, 5); // we start to read registers a "0xDF01",
                                       // and we finally read 5 registers

  for (var i = 0; i < after.length; i++) {
    gollumPrint("Value read: " + after[i].toString(16));
  }
}

main();
