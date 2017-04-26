function main()
{
 var baseFreq = 434000000;
 var rate = 25449;
 var mod = 0x30;

 gollumPrint("-- Start RF jamming --");
 gollumRfStartRfJamming(baseFreq, rate, mod);

 gollumRfSleep(6000000); // in microseconds

 gollumRfStopRfJamming();
 gollumPrint("-- Stop RF jamming --");

}


main();