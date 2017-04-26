function main()
{
  //MOD_2FSK = 0x00
  //MOD_4FSK = 0x40
  //MOD_GFSK = 0x10
  //MOD_ASK_OOK = 0x30
  //MOD_MSK = 0x70
  gollumRfSetModulation(0x30);
}


main();
