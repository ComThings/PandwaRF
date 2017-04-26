/********************************************************
 * JavaScript Configuration File for IDK Wireless chime CAR_87                        *
 ********************************************************
 * Author : D. ELAIDI                                    *
 ********************************************************
 * Creation Date : 18/07/2015                         *
 ********************************************************/

var devicename = "Wireless chime CAR_87";
var manufacter = "IDK";

var IDK_CAR_87_JUMPER_LENGTH = 8;
var IDK_CAR_87_MESSAGE_LENGTH = 12;
var IDK_CAR_87_NB_CODEWORD = 4;

var IDK_CAR_87_DATA_1 = 0x8E;
var IDK_CAR_87_DATA_0 = 0x88;
var IDK_CAR_87_DATA_F= 0xEE;

var idk_car_87_data_sync = [ 0x80, 0x00, 0x00, 0x00 ];


// Convert long device code into binary
function ism_dec2binary( value, length )
{
  var i, pow2;
  var str = "";

  for (i = 0; i < length; i++){
      pow2 = Math.pow(2, (length - 1 - i));
      if (Math.floor(value / pow2) == 1){
            value -= pow2;
            str += "1";
    } else {
            str += "0";
    }
  }

  return str;
}

function idk_car_87_bit_to_symbol( c )
{
  if (c == '1')
    return IDK_CAR_87_DATA_1;
  else if (c == '0')
    return IDK_CAR_87_DATA_0;
  else if (c == 'f')
    return IDK_CAR_87_DATA_F;

  return (0xAA);
}

function idk_car_87_get_message_string( jumper )
{
  var length, i;
  var str;
  var njumper = 255 - jumper; //Logic is inverted: no jumper means "1"
  var strnJumper;
  var idk_car_87_message = new Array();

  /* Convert jumper to binary string */
  strnJumper = ism_dec2binary(njumper, IDK_CAR_87_JUMPER_LENGTH);

  /* LSB is sent first, so invert the string */
  for (i = 0; i < IDK_CAR_87_JUMPER_LENGTH; i++)
    idk_car_87_message[i] = strnJumper[IDK_CAR_87_JUMPER_LENGTH - 1 - i];

  /* Fixed pattern 1111 */
  idk_car_87_message[IDK_CAR_87_JUMPER_LENGTH + 0] = "1";
  idk_car_87_message[IDK_CAR_87_JUMPER_LENGTH + 1] = "1";
  idk_car_87_message[IDK_CAR_87_JUMPER_LENGTH + 2] = "1";
  idk_car_87_message[IDK_CAR_87_JUMPER_LENGTH + 3] = "1";

  return idk_car_87_message;
}


function main()
{
  var message = new Array();
  var codeword = new Array();
  var loop, i;

  //gollumOpen();    Not needed
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

  //gollumClose();    	Not needed
}

main();

/********************************************************
 * End of Javascript Configuration File                 *
 ********************************************************/
