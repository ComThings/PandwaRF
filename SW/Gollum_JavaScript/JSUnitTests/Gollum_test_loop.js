/********************************************************
 * JavaScript Configuration File for IDK Wireless chime CAR_87                        *
 ********************************************************
 * Author : D. ELAIDI                                    *
 ********************************************************
 * Creation Date : 18/07/2015                         *
 ********************************************************/

var devicename = "Wireless chime CAR_87";
var manufacter = "IDK";

                         
function main()
{
  var message = new Array();
  var codeword = new Array();
  var loop, i;

  gollumRfTxFlush();

  gollumRfSetRfConfigPredefined(0);

  message = idk_car_87_get_message_string(21);
  //console.log(message);

  for(loop = 0; loop < IDK_CAR_87_NB_CODEWORD; loop++) {
    /* Sync */
    gollumRfSendStream(idk_car_87_data_sync);

    /* Code word */
    //Convert...
    for (i = 0; i < IDK_CAR_87_MESSAGE_LENGTH; i++)
      codeword[i] = idk_car_87_bit_to_symbol(message[i]);
    //... and send
    gollumRfSendStream(codeword);
  }     
}

main();  
