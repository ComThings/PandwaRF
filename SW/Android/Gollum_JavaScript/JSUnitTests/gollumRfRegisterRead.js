function main()
{


  var s = gollumRfRegisterRead(0xDF01, 5); // we start to read registers a "0xDF01",
                                           // and we finally read 5 registers
  var i;
  for(i = 0; i < s.length; i++){
    gollumPrint("Value read - "+ s[i].toString(16));
  }

}


main();
