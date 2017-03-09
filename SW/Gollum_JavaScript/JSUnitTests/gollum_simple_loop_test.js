function main()
{   
  for(loop = 0; loop < 10; loop++) {  
  	gollumRfSetFrequency(430000000 + loop * 100000);
  }
}


main();
