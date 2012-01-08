int E1 = 6;   
int M1 = 7;
int E2 = 5;
int M2 = 4;

void setup() { 
  pinMode(M1, OUTPUT);
  pinMode(M2, OUTPUT);
  
  pinMode(9, OUTPUT);
  pinMode(10, OUTPUT);
  pinMode(11, OUTPUT);
  pinMode(13, OUTPUT);
  Serial.begin(9600);
} 

void loop() {
  if(Serial.available()) {
    delay(10);
    byte c = Serial.read();
    if(c == 'A') {
      setAnalog();
    } else if(c == 'D') {
      setDigital();
    }
  }
}

int readNumber(int length) {
  int number = 0;
  for(int i=0;i<length;i++) {
    int d = Serial.read();
    d = d - '0';
    if(d<0 || d>9) {
      return -1;
    }
    number *= 10;
    number += d;
  }
  return number;
}

void setAnalog() {
  int pin = readNumber(2);
  if(pin == -1)
    return;
  int value = readNumber(3);
  if(value == -1)
    return;
  Serial.print("setAnalog(");
  Serial.print(pin);
  Serial.print(", ");
  Serial.print(value);
  Serial.println(")");
  analogWrite(pin, value);
}

void setDigital() {
  int pin = readNumber(2);
  if(pin == -1)
    return;
  int value = readNumber(1);
  if(value == -1)
    return;
  Serial.print("setDigital(");
  Serial.print(pin);
  Serial.print(", ");
  Serial.print(value);
  Serial.println(")");
  digitalWrite(pin, value == 1 ? HIGH : LOW);
}
