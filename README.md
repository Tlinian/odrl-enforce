# ODRL-lib

[![Maven Central](https://img.shields.io/badge/Maven%20Central-v1.0.0-green)](https://central.sonatype.com/artifact/es.upm.fi.oeg/odrl-lib/) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 

The ODRL-lib is a maven dependecy that provides support to the current [ODRL specification](https://www.w3.org/TR/odrl-model/). Check the [wiki](https://github.com/oeg-upm/odrl-lib/wiki) for detailed documentation.

## Quick start 

#### example

```
{
  "@context": "http://www.w3.org/ns/odrl.jsonld",
  "@type": "Policy",
  "uid": "https://upm.es/policy/6",
  "permission": [{
    "target": "https://jsonplaceholder.typicode.com/users/1",
    "action": ["read", "process", "download"],
    "constraint": [{
      "leftOperand": "odrl:dateTime",
      "operator": "gt",
      "rightOperand":  { "@value": "2022-01-01T06:00:13.625668Z", "@type": "xsd:dateTime" },
      "dct:comment": "生效于至2022年初"
    },{
      "leftOperand": "dateTime",
      "operator": "lt",
      "rightOperand":  { "@value": "2025-12-30T06:00:13.625668Z", "@type": "xsd:dateTime" },
      "dct:comment": "截止于至2025年底"
    },{
      "operator": "odrl:or",
      "dct:comment": "who? 限制使用团队列表、用户列表、限制某个用户",
      "constraint": [
        {
          "leftOperand": {
            "@value": "partyRoleTeam",
            "@type": "xsd:string"
          },
          "operator": "odrl:isAnyOf",
          "rightOperand": [
            {"@value": "teamA", "@type": "xsd:string"},
            {"@value": "teamB", "@type": "xsd:string"},
            {"@value": "teamC", "@type": "xsd:string"}],
          "dct:comment": "使用者必须属于teamA、teamB、teamC团队列表"
        },
        {
          "leftOperand": {
            "@value": "userA",
            "@type": "xsd:string"
          },
          "operator": "odrl:isAnyOf",
          "rightOperand": [
            {"@value": "userA", "@type": "xsd:string"},
            {"@value": "userB", "@type": "xsd:string"},
            {"@value": "userC", "@type": "xsd:string"}],
          "dct:comment": "使用者必须属于userA、userB、userC用户列表"
        },
        {
          "leftOperand":  {
            "@value": "userId",
            "@type": "xsd:string"
          },
          "operator": "eq",
          "rightOperand": "userId",
          "dct:comment": "限制某个用户使用"
        }
      ]
    },
      {
        "leftOperand": { "@value": "6", "@type": "xsd:string" },
        "operator": "lteq",
        "rightOperand": "5",
        "dct:comment": "使用者最多使用资源5次"
      },
      {
        "leftOperand": { "@value": "appA", "@type": "xsd:string" },
        "operator": "odrl:isAnyOf",
        "rightOperand": [
          {"@value": "appA", "@type": "xsd:string"},
          {"@value": "appB", "@type": "xsd:string"},
          {"@value": "appC", "@type": "xsd:string"}],
        "dct:comment": "限制使用的应用列表"
      }]
  }]
}
```

#### Enforce ODRL policies

```

public class Main {
    public static void main(String[] args) throws UnsupportedFunctionException, OperandException, EvaluationException, OdrlRegistrationException, OperatorException, IllegalAccessException {
        OdrlLib odrl = new OdrlLib();
        String policy = readPolicy("data/policy.json");
        System.out.println(odrl.solveResultToJson(policy));
    }

    public static String readPolicy(String name) {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(name), StandardCharsets.UTF_8)) {

            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }
}
```



## Supported features

### ODRL operantors

| Operators                                        | Implementation status | #       |
| ------------------------------------------------ | --------------------- | ------- |
| [eq](http://www.w3.org/ns/odrl/2/eq)             | supported             | &check; |
| [gt](http://www.w3.org/ns/odrl/2/gt)             | supported             | &check; |
| [gteq](http://www.w3.org/ns/odrl/2/gteq)         | supported             | &check; |
| [lt](http://www.w3.org/ns/odrl/2/lt)             | supported             | &check; |
| [lteq](http://www.w3.org/ns/odrl/2/lteq)         | supported             | &check; |
| [neq](http://www.w3.org/ns/odrl/2/neq)           | supported             | &check; |
| or                                               | supported             | &check; |
| [hasPart](http://www.w3.org/ns/odrl/2/hasPart)   | unsupported           | &cross; |
| [isA](http://www.w3.org/ns/odrl/2/isA)           | unsupported           | &cross; |
| [isAllOf](http://www.w3.org/ns/odrl/2/isAllOf)   | unsupported           | &cross; |
| [isAnyOf](http://www.w3.org/ns/odrl/2/isAnyOf)   | supported             | &check; |
| [isNoneOf](http://www.w3.org/ns/odrl/2/isNoneOf) | unsupported           | &cross; |
| [isPartOf](http://www.w3.org/ns/odrl/2/isPartOf) | unsupported           | &cross; |

### ODRL operands

* The available implemented [Left Operands](http://www.w3.org/ns/odrl/2/LeftOperand) from those specified in the [ODRL Vocabulary & Expression 2.2](https://www.w3.org/ns/odrl/2/) are the following:

| Left Operands                                                | Implementation status | #       |
| ------------------------------------------------------------ | --------------------- | ------- |
| [absolutePosition](http://www.w3.org/ns/odrl/2/dateTimeabsolutePosition) | unsupported           | &cross; |
| [absoluteSize](http://www.w3.org/ns/odrl/2/dateTimeabsoluteSize) | unsupported           | &cross; |
| [absoluteSpatialPosition](http://www.w3.org/ns/odrl/2/dateTimeabsoluteSpatialPosition) | unsupported           | &cross; |
| [absoluteTemporalPosition](http://www.w3.org/ns/odrl/2/dateTimeabsoluteTemporalPosition) | unsupported           | &cross; |
| [count](http://www.w3.org/ns/odrl/2/dateTimecount)           | unsupported           | &cross; |
| [dateTime](http://www.w3.org/ns/odrl/2/dateTime)             | supported             | &check; |
| [delayPeriod](http://www.w3.org/ns/odrl/2/delayPeriod)       | unsupported           | &cross; |
| [deliveryChannel](http://www.w3.org/ns/odrl/2/deliveryChannel) | unsupported           | &cross; |
| [device](http://www.w3.org/ns/odrl/2/device)                 | unsupported           | &cross; |
| [elapsedTime](http://www.w3.org/ns/odrl/2/elapsedTime)       | unsupported           | &cross; |
| [event](http://www.w3.org/ns/odrl/2/event)                   | unsupported           | &cross; |
| [fileFormat](http://www.w3.org/ns/odrl/2/fileFormat)         | unsupported           | &cross; |
| [industry](http://www.w3.org/ns/odrl/2/industry)             | unsupported           | &cross; |
| [language](http://www.w3.org/ns/odrl/2/language)             | unsupported           | &cross; |
| [media](http://www.w3.org/ns/odrl/2/media)                   | unsupported           | &cross; |
| [meteredTime](http://www.w3.org/ns/odrl/2/meteredTime)       | unsupported           | &cross; |
| [payAmount](http://www.w3.org/ns/odrl/2/payAmount)           | unsupported           | &cross; |
| [percentage](http://www.w3.org/ns/odrl/2/percentage)         | unsupported           | &cross; |
| [product](http://www.w3.org/ns/odrl/2/product)               | unsupported           | &cross; |
| [purpose](http://www.w3.org/ns/odrl/2/purpose)               | unsupported           | &cross; |
| [recipient](hhttp://www.w3.org/ns/odrl/2/recipient)          | unsupported           | &cross; |
| [relativePosition](http://www.w3.org/ns/odrl/2/relativePosition) | unsupported           | &cross; |
| [relativeSize](http://www.w3.org/ns/odrl/2/relativeSize)     | unsupported           | &cross; |
| [relativeSpatialPosition](http://www.w3.org/ns/odrl/2/relativeSpatialPosition) | unsupported           | &cross; |
| [relativeTemporalPosition](http://www.w3.org/ns/odrl/2/relativeTemporalPosition) | unsupported           | &cross; |
| [resolution](http://www.w3.org/ns/odrl/2/resolution)         | unsupported           | &cross; |
| [spatial](hhttp://www.w3.org/ns/odrl/2/spatial)              | unsupported           | &cross; |
| [spatialCoordinates](http://www.w3.org/ns/odrl/2/spatialCoordinates) | unsupported           | &cross; |
| [system](http://www.w3.org/ns/odrl/2/system)                 | unsupported           | &cross; |
| [systemDevice](http://www.w3.org/ns/odrl/2/systemDevice)     | unsupported           | &cross; |
| [timeInterval](http://www.w3.org/ns/odrl/2/timeInterval)     | unsupported           | &cross; |
| [unitOfCount](http://www.w3.org/ns/odrl/2/unitOfCount)       | unsupported           | &cross; |
| [version](http://www.w3.org/ns/odrl/2/version)               | unsupported           | &cross; |
| [virtualLocation](http://www.w3.org/ns/odrl/2/virtualLocation) | unsupported           | &cross; |