function main() {

  var values;
  var names;
  var i;
  var res = gollumGetCC1111RfRegisters();

  names = res[0];
  values = res[1];
  for (i = 0; i < names.length; i++) {
    gollumPrint("" + names[i] + ": " + values[i].toString(16));
  }
}
main();