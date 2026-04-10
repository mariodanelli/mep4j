# mep4j — Math Expression Parser for Java

**MEP4J** is a high-performance math expression string parser for Java (version >= 8).

## Maven Dependency

```xml
<dependency>
  <groupId>io.github.mariodanelli</groupId>
  <artifactId>mep4j</artifactId>
  <version>1.0.1</version>
</dependency>
```

## Supported Operators

`+` &nbsp; `-` &nbsp; `*` &nbsp; `/` &nbsp; `%`

## Supported Functions

| Function | Description | Syntax |
|----------|-------------|--------|
| `abs` | Absolute value | `abs(<value>)` |
| `cos` | Cosine | `cos(<radians>)` |
| `sin` | Sine | `sin(<radians>)` |
| `cosh` | Hyperbolic cosine | `cosh(<radians>)` |
| `sinh` | Hyperbolic sine | `sinh(<radians>)` |
| `acos` | Arccosine | `acos(<value>)` |
| `asin` | Arcsine | `asin(<value>)` |
| `tan` | Tangent | `tan(<radians>)` |
| `tanh` | Hyperbolic tangent | `tanh(<radians>)` |
| `atan` | Arc tangent | `atan(<value>)` |
| `atan2` | Polar angle θ from rectangular coordinates | `atan2(<x>, <y>)` |
| `sqrt` | Square root | `sqrt(<value>)` |
| `cbrt` | Cube root | `cbrt(<value>)` |
| `root` | Nth root | `root(<value>, <root>)` |
| `log` | Natural logarithm (base *e*) | `log(<value>)` |
| `log10` | Base-10 logarithm | `log10(<value>)` |
| `log1p` | Natural logarithm of (argument + 1) | `log1p(<value>)` |
| `exp` | *e* ^ argument | `exp(<value>)` |
| `expm1` | (*e* ^ argument) − 1 | `expm1(<value>)` |
| `pow` | *a* ^ *b* | `pow(<a>, <b>)` |

The library also supports **custom variables** and the pre-loaded constants **`e`** and **`pi`** (case-insensitive).

## Usage Examples

### 1. No variables

```java
import net.sourceforge.mep4j.core.MathParser;
import net.sourceforge.mep4j.core.MathParserException;

double result = new MathParser().parse("2 * 4 - 5").execute();
```

### 2. With a variable

```java
import net.sourceforge.mep4j.core.MathParser;
import net.sourceforge.mep4j.core.MathParserException;

double result = new MathParser()
    .addVariable("S", 2.0)
    .parse("s * 4 - 21")
    .execute();
```

### 3. Multiple executions (changing variable values)

```java
import net.sourceforge.mep4j.core.MathParser;
import net.sourceforge.mep4j.core.MathParserException;

int numVariations = 100;
int initValue = 10;

MathParser mathParser = new MathParser();
mathParser.addVariable("x", initValue);
mathParser.parse("2 + (7 - 5) * 3.14159 * pow(x, (12-10)) + sin(-3.141)");

for (int i = initValue; i < numVariations; ++i) {
    Double result = mathParser.execute();
    mathParser.addVariable("x", i);
}
```

## API Reference

### Class `MathParser`

| Method | Description |
|--------|-------------|
| `static String getVersion()` | Returns the version description |
| `MathParser()` | Constructor — instantiates the parser |
| `MathParser addVariable(String varName, Long value)` | Adds a Long variable and returns the `MathParser` instance (chainable) |
| `MathParser putVariable(String varName, Double value)` | Adds a Double variable and returns the `MathParser` instance (chainable) |
| `Double getVariable(String varName)` | Returns the value of a previously set variable, or `null` |
| `MathParser parse(String expression)` | Parses the math expression; call after `addVariable` if variables are used (chainable) |
| `Double execute()` | Executes the previously parsed expression; returns `Double.NaN` on error |
| `MathParserException getLastException()` | Returns the exception from the last failed `execute()` or `parse()` call |
