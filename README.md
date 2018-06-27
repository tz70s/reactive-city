# reactive-city
Congestion and emergency routing CQRS system.

## Usage
Not finished, yet.

### Configuration passing
For each instances (a.k.a actor-system) will be passed configuration by program arguments, in which we can pass it when starting containers.

values:
* location: one of `cloud`, `fog-east`, `fog-west`, this is essential for dynamic flow migration.