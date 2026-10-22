apply plugin: 'java-library'

dependencies {
  api "com.badlogicgames.gdx:gdx:$gdxVersion"
  api "com.fazecast:jSerialComm:$jSerialCommVersion"
  testImplementation 'junit:junit:4.13.2'
}
