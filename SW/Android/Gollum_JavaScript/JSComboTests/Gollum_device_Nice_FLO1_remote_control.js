function main() {
  var mod = 0x30;
  var invert = -1;
  var rate = 1500;
  var word = "\xB2\xCB\x2C\xB2\xC8\x00\x00\x00\x00";
  gollumRfSetupConfig();
  gollumRfInitDongle();
  gollumRfSetModulation(mod, invert);
  gollumRfSetBitRate(rate);
  gollumRfSetMaxPower(0);
  gollumRfXmitAsync(word, 10, 0, 0, 1);
}

main();