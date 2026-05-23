# Final Presentation Readiness - S33

This file is the short defense pack for the final presentation.

## 1. Final feature inventory

- S28: active Prometheus alert rules
- S29: `Idempotency-Key` support for critical write endpoints
- S30: transactional outbox for enrollment creation and billing status changes
- S31: basic saga compensation by choreography when billing becomes `CANCELLED`
- S32: isolated PostgreSQL streaming replication demo with read replica and manual promotion
- S33: docs-only final cleanup plus a unified regression and evidence package

## 2. What is implemented

- centralized PostgreSQL for the main application stack
- Redis cache in `course-service`
- RabbitMQ exchange, queues, bindings, and event consumers
- Prometheus scraping and active alert rules
- Grafana reachable as infrastructure
- idempotency records for critical enrollment and billing writes
- transactional outbox for reliable producer-side event publication
- choreography-based compensation from billing cancellation to enrollment cancellation
- HAProxy application failover and switchover for `course-service`
- local backup and restore scripts for PostgreSQL
- isolated S32 PostgreSQL primary plus replica demo with manual failover
- k6 smoke, load, and concurrency assets

## 3. What is demo-only

- HAProxy application failover is a focused local demo for `course-service`, not a platform-wide failover mesh
- the S32 PostgreSQL replication topology is isolated from the main app stack
- the S32 database failover flow is manual and operator-driven
- Grafana is available infrastructure, not a finished business dashboard package
- k6 assets are prepared scripts until they are executed and captured as evidence

## 4. What remains future production work

- Kubernetes or another production orchestrator
- automatic PostgreSQL failover with leader election and fencing
- application-integrated read routing to PostgreSQL replicas
- Redis cluster and RabbitMQ cluster
- platform-wide HA coverage for all services behind a unified entrypoint
- full saga engine with orchestration, DLQ strategy, and richer compensation policies
- scheduled backups, off-site storage, encryption, and tested retention policy
- Alertmanager routing and production-grade dashboarding

## 5. Exact language to avoid overclaiming

Do not say:

- `Tenemos alta disponibilidad productiva completa.`
- `PostgreSQL hace failover automatico.`
- `La replica PostgreSQL ya alimenta a los microservicios.`
- `Grafana ya esta listo como observabilidad completa de negocio.`
- `Esto es un motor de sagas completo.`

Say instead:

- `Tenemos una plataforma local HA-ready con demos concretos de continuidad y recuperacion.`
- `El stack principal sigue usando PostgreSQL centralizado; el failover automatico de base de datos no esta implementado.`
- `La replica PostgreSQL de S32 es un demo aislado para evidenciar streaming replication y promocion manual.`
- `Grafana esta disponible como infraestructura, pero los dashboards productivos siguen pendientes.`
- `La compensacion actual es una saga basica por choreografia, no un framework completo de sagas.`

## 6. Short Q and A defense

### Kubernetes not implemented

Answer:

`No esta implementado. El proyecto se presenta sobre Docker Compose porque el alcance academico prioriza base de datos, mensajeria, observabilidad y continuidad demostrable antes que una migracion completa de plataforma.`

### Automatic database failover not implemented

Answer:

`No esta implementado. El stack principal mantiene PostgreSQL centralizado y su recuperacion actual sigue siendo backup y restore. El unico failover de base de datos que demostramos hoy es el flujo manual del demo aislado S32.`

### PostgreSQL replication demo is isolated

Answer:

`Si. S32 es un demo aislado. Usa su propio Compose, su propio primary y su propia replica. No reemplaza la base principal ni reconfigura las conexiones JDBC de los microservicios.`

### Manual failover vs automatic failover

Answer:

`El failover manual requiere detener el primary, promover la replica y mover el cliente de forma operativa. El failover automatico requeriria deteccion, eleccion de lider, fencing, reroute y procedimientos probados que este repo no implementa.`

### Choreography vs orchestration

Answer:

`Aqui usamos choreografia. Los servicios reaccionan a eventos publicados en RabbitMQ sin un orquestador central. Orquestacion implicaria un coordinador explicito que gobierna el flujo distribuido.`

### Saga compensation vs full saga engine

Answer:

`La compensacion actual cubre un caso concreto: si billing queda CANCELLED, enrollment se cancela por evento. No incluye un motor general de sagas, orquestador, DLQ, ni politicas avanzadas de compensacion.`

### Outbox vs direct RabbitMQ publish

Answer:

`El outbox persiste la intencion del evento dentro de la misma transaccion de negocio antes de publicar. Eso reduce el riesgo de perder eventos frente a un publish directo desacoplado de la escritura principal.`

### Idempotency keys

Answer:

`Los Idempotency-Key permiten repetir de forma segura ciertas escrituras criticas sin duplicar la operacion. En este repo estan implementados para POST de enrollments y billings, no para todos los endpoints.`

### Backups vs replication

Answer:

`Backups y replicacion resuelven problemas distintos. Los backups ayudan a recuperar datos historicos despues de perdida o corrupcion. La replicacion ayuda a continuidad y lectura sobre otra instancia, pero no reemplaza una estrategia de backup.`

### Grafana availability vs full production dashboarding

Answer:

`Grafana esta disponible y accesible como infraestructura. Lo que no afirmamos es que el repo ya entregue dashboards de negocio, alert routing o gobierno completo de observabilidad de produccion.`
