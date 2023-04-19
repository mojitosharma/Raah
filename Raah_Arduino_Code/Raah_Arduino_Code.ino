#include <CapacitiveSensor.h>

// String message to check when to send and stopsending data 
int msg;

// Defining variables for touch sensor 
CapacitiveSensor number0 = CapacitiveSensor(34,35);
CapacitiveSensor number1 = CapacitiveSensor(36,37);
CapacitiveSensor number2 = CapacitiveSensor(38,39);
CapacitiveSensor number3 = CapacitiveSensor(40,41);
CapacitiveSensor number4 = CapacitiveSensor(42,43);
CapacitiveSensor number5 = CapacitiveSensor(44,45);
CapacitiveSensor number6 = CapacitiveSensor(46,47);
CapacitiveSensor number7 = CapacitiveSensor(48,49);
CapacitiveSensor number8 = CapacitiveSensor(50,51);
CapacitiveSensor number9 = CapacitiveSensor(52,53);

// store the average value
long average[10];
long value[10];
// store current state 
int current_State[10];
//current number
int current_number = 0;


void setup(){
  // Intitializing 
  Serial.begin(9600);       // Set the data rate in bits per second (baud)
  //intialize the values 
  for(int i=0; i<10; i++){
      average[i] = 0;
      value[i] = 1000;
      current_State[i] = 0;
  }
}

void loop(){
  // If there is anything to read then read and store it in variable msg
  // msg can be 1 to "<turn on>" or 0 to "<turn off>"
 if (Serial.available()) { // If data is available to read
    msg = Serial.parseInt(); // Read the incoming integer
    current_number = 0;     // start from 0 
 }


  long n[10];
  // read the values of the capacitivesensor
  n[0] = (long)number0.capacitiveSensor(20);
  n[1] = (long)number1.capacitiveSensor(20);
  n[2] = (long)number2.capacitiveSensor(20);
  n[3] = (long)number3.capacitiveSensor(20);
  n[4] = (long)number4.capacitiveSensor(20);
  n[5] = (long)number5.capacitiveSensor(20);
  n[6] = (long)number6.capacitiveSensor(20);
  n[7] = (long)number7.capacitiveSensor(20);
  n[8] = (long)number8.capacitiveSensor(20);
  n[9] = (long)number9.capacitiveSensor(20);
  
  for(int i=0; i<10; i++){
    // update the average value 
    // average is a weighted number with 0.125 weight given to current value 
    average[i] = 0.875*average[i] + 0.125*n[i]; 
    // if the current_Value is 4 times the average and current_State is 1 (indicating not on [ik should have been 0])
    // and it should be atleast one-tenth of last value when touched 
    if(n[i] > 4*average[i] && current_State[i] == 1 && n[i] > value[i]/10){
      // changing current state indicating we got a hit
      current_State[i] = 0;
      // updating value to maximum on current value and last value 
      value[i] = n[i]>value[i]?n[i]:value[i];
    }
    else{
      // change current_State to 1 (indicating not on)
      current_State[i] = 1;
    }
  }

  // flag store the value of any other number that was touched other then last number 
  int flag = -1;
  for(int i=0; i<10; i++){
    if(i != current_number-1 && current_State[i] == 0){
      flag = i;
    }
  }

  // if the system is on then send the flag {flag stored value of the greatest number that was touched
  if(msg == 1 && flag > -1){
      Serial.print(flag); // Send -1 back to the Bluetooth module indicating incorrect value
      Serial.flush();
  }
}
