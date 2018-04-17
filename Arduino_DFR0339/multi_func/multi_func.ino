#include <stdarg.h>
 
 #define PIN_VIB_LED 4
 #define PIN_VIB_MOTOR 5
 
 #define LOOP_DELAY 10 // ms
 #define INPUT_PARAM_BUF 6
 #define MAX_INPUT_PARAMS 6
 #define SPLITTER ","

// --------------- Constants --------------- //

enum OpMode {STANDBY, SHOCK, DETECTION, OpModeElemsCount};
enum OpCode {CHANGE_MODE, SET_PARAMS, OpCodeElemsCount};

// ----------------- States ---------------- //

OpMode currentMode = STANDBY;
bool shockEnabled = false;
bool detectionEnabled = false;
bool vibMotorOn = false;
byte vibMotorStrength = 60; // 0-255

// ----------------- Function Declarations ---------------- //

void readInput();
void setModeParams(OpMode targetMode, int paramCount, int params[]);
void reportStatus();
void vibMotorPwm(bool on, byte strength);

void modePrint(OpMode currentMode);
void modePrint(OpMode currentMode, int n_args, ...);
bool checkCounter(int *counter, int execMod);


void setup() {
  Serial.begin(115200);
  pinMode(PIN_VIB_LED, OUTPUT);
  pinMode(PIN_VIB_MOTOR, OUTPUT);
}

void loop() {  
  readInput();
  reportStatus();
  vibMotorPwm(vibMotorOn, vibMotorStrength);

  delay(LOOP_DELAY);
}


void readInput() {
  static int tokens[MAX_INPUT_PARAMS];
  
  if (!Serial.available())
    return;

  String input = Serial.readString();

  // Parse tokens
  int tokenCount = 0;
  char* stoken = strtok((char*) input.c_str(), SPLITTER);
  while (stoken != 0) {
    tokens[tokenCount++] = atoi(stoken);
    stoken = strtok(0, SPLITTER);
  }

  // Perform operation according to the input
  switch(tokens[0]) {
    case CHANGE_MODE: // 0
      // input: 1,<mode_ordinal>
      if (tokenCount == 2 && tokens[1] < OpModeElemsCount) {
        currentMode = (OpMode) tokens[1];
      }
      break;
      
    case SET_PARAMS: // 1
      // input: 2,<mode_ordinal>,<parm1>,<parm2>...
      if (tokenCount >= 3 && tokens[1] < OpModeElemsCount) {
        setModeParams((OpMode) tokens[1], tokenCount - 2, &tokens[2]);
      }
      break;
  }
}

void setModeParams(OpMode targetMode, int paramCount, int params[]) {
  switch (targetMode) {
    case STANDBY: // 0
      break;
      
    case SHOCK: // 1
      // params: <enable>
      if (paramCount == 1) {
        shockEnabled = (params[0] == 1);
      }
      break;
 
    case DETECTION: // 2
      // params: <enable>
      if (paramCount == 1) {
        detectionEnabled = (params[0] == 1);
      }
      break;
  }
}

void reportStatus() {
  static int counter = 0;
  const int execMod = 1000 / LOOP_DELAY; // 1 sec. interval
  
  if (!checkCounter(&counter, execMod))
    return;
  
  switch (currentMode) {
    case STANDBY: // 0
      // output: 0
      modePrint(STANDBY);
      break;

    case SHOCK: // 1
      // output: 1,<enabled>
      modePrint(SHOCK, 2, shockEnabled);
      break;
 
    case DETECTION: // 2
      // output: 2,<enabled>,<emg_raw_value: 0-255>
      // ex: 2,1,100
      modePrint(DETECTION, 3, detectionEnabled, analogRead(0));
      break;
  }
}

void vibMotorPwm(bool on, byte strength) {
  analogWrite(PIN_VIB_MOTOR, on & strength);
}


// ----------------- Utility Functions ---------------- //

void modePrint(OpMode currentMode) {
  Serial.println(currentMode);
}

void modePrint(OpMode currentMode, int n_args, ...) {
  va_list ap;
  va_start(ap, n_args);

  Serial.print(currentMode);
  for(int i = 2; i <= n_args; i++) {
      Serial.print(SPLITTER);
      Serial.print(va_arg(ap, int));
  }
  Serial.println();
  
  va_end(ap);
}

bool checkCounter(int *counter, int execMod) {
  bool result = *counter % execMod == 0;
  
  if (++(*counter) > 32000)
    *counter = 0;

  return result;
}

