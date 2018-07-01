# reactive-city
Congestion and emergency routing CQRS system.

## Usage
```bash
# Simplest build and execution.
# Can modify the configurations in docker-compose.yml
docker-compose up -d
```

### Configuration passing
For each instances (a.k.a actor-system) will be passed configuration by program arguments, in which we can pass it when starting containers.

values:
* location: one of `cloud`, `fog-east`, `fog-west`, this is essential for flow recognition.