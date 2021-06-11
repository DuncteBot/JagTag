## JagTag
JagTag is a simple - yet powerful and customizable - interpretted text parsing language!
Some methods are included in the built-in libraries, and additional methods can be defined that utilize the parser environment veriables, as well as the method's input.

## Simple Example
```java
import com.jagrosh.jagtag.*;
public class Example
{
  public static void main(String[] args)
  {
    Parser parser = JagTag.newDefaultBuilder()
                .addMethod( new Method("exclaim", (env,in) -> in[0]+"!!!") )
                .build();
    String result = parser.parse("{exclaim:{if:this|=|that|then:Foo Bar|else:Hello World}}");
    System.out.println(result);
  }
}
```
Result: `Hello World!!!`

## Gradle
```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.DuncteBot:JagTag:Tag")
}
```

## Maven
To use Maven with JagTag, simply add the following sections to your pom.xml
```xml
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
```xml
  <dependency>
    <groupId>com.github.DuncteBot</groupId>
    <artifactId>JagTag</artifactId>
    <version>Tag</version>
  </dependency>
```

## Other Libraries
Below are JagTag-related libraries available for other languages or purposes:
* [**TheSharks/JagTag-JS**](https://github.com/TheSharks/JagTag-JS) - A JavaScript port of the JagTag text parsing language
* [**TheMonitorLizard/JagTagXML**](https://github.com/TheMonitorLizard/JagTagXML) - a JagTag to XML transpiler written in Java
