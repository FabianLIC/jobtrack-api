# Guía técnica — Proyecto JobTrack API

Documento de referencia personal. Aquí está qué es cada tecnología del proyecto,
por qué se usa, y los comandos que necesitas.

Objetivo: poder defender cada decisión del proyecto en una entrevista técnica.

---

## Índice

1. [El stack en una frase](#1-el-stack-en-una-frase)
2. [Java y el JDK](#2-java-y-el-jdk)
3. [Maven](#3-maven)
4. [Spring y Spring Boot](#4-spring-y-spring-boot)
5. [Tomcat embebido](#5-tomcat-embebido)
6. [API REST y JSON](#6-api-rest-y-json)
7. [Anotaciones de Spring que ya has usado](#7-anotaciones-de-spring-que-ya-has-usado)
8. [JPA, Hibernate y Spring Data JPA](#8-jpa-hibernate-y-spring-data-jpa)
9. [PostgreSQL](#9-postgresql)
10. [Docker](#10-docker)
11. [Git y GitHub](#11-git-y-github)
12. [Comandos de Git — chuleta completa](#12-comandos-de-git--chuleta-completa)
13. [De GitHub Desktop a la línea de comandos](#13-de-github-desktop-a-la-línea-de-comandos)
14. [Tecnologías que llegarán más adelante](#14-tecnologías-que-llegarán-más-adelante)
15. [Preguntas de entrevista y cómo responderlas](#15-preguntas-de-entrevista-y-cómo-responderlas)
16. [Registro de decisiones del proyecto](#16-registro-de-decisiones-del-proyecto)

---

## 1. El stack en una frase

Una **API REST** escrita en **Java** con **Spring Boot**, que guarda datos en
**PostgreSQL** a través de **JPA/Hibernate**, se construye con **Maven**, se
versiona con **Git**, y se ejecuta en contenedores con **Docker**.

Cada pieza resuelve un problema concreto. Ninguna está por moda.

---

## 2. Java y el JDK

**Qué es.** Java es el lenguaje de programación. El **JDK** (*Java Development
Kit*) es el paquete de herramientas para desarrollar en Java. Incluye:

- `javac` — el compilador. Traduce tu código `.java` a **bytecode** (`.class`).
- La **JVM** (*Java Virtual Machine*) — ejecuta ese bytecode.
- Librerías estándar y utilidades.

**Por qué el bytecode importa.** El bytecode no es código máquina de tu
procesador: es un formato intermedio que la JVM interpreta. Eso es lo que hace
que Java sea portable — "compila una vez, ejecuta en cualquier sitio". El mismo
`.jar` funciona en Windows, Linux o macOS mientras haya una JVM.

**Versiones LTS.** Java saca una versión nueva cada 6 meses, pero solo algunas
son **LTS** (*Long Term Support*), con soporte de varios años. Las empresas usan
LTS: Java 8 (legacy), 11, 17, 21 y 25. Java 17 y 21 son lo que más verás en
ofertas de empleo ahora mismo.

**En tu proyecto.** Tienes el **JDK 25** instalado, pero el proyecto compila
apuntando a **Java 21** (`<java.version>21</java.version>` en el `pom.xml`).

*¿Por qué?* Porque Java 21 es lo que piden la mayoría de ofertas, y un JDK más
nuevo puede compilar código dirigido a versiones anteriores sin problema. En los
logs de compilación se ve como `[debug parameters release 21]`.

---

## 3. Maven

**El problema que resuelve.** Sin una herramienta de construcción, para usar una
librería tendrías que: buscar el `.jar`, descargarlo, meterlo en el proyecto,
descubrir que esa librería necesita otras tres, repetir el proceso... y
documentar todo para que un compañero pueda reproducirlo.

**Cómo lo resuelve.** Declaras qué necesitas en el `pom.xml` y Maven se encarga
del resto: descarga las librerías desde repositorios públicos (Maven Central),
resuelve las dependencias de tus dependencias (*dependencias transitivas*), y
compila el proyecto siguiendo un ciclo de vida estándar.

**El `pom.xml`.** *Project Object Model*. Es el archivo central del proyecto:
identidad (groupId, artifactId, version), versión de Java, dependencias y
plugins.

**El Maven Wrapper (`mvnw` / `mvnw.cmd`).** Un script que descarga y usa una
versión concreta de Maven, así que:

- No necesitas instalar Maven en tu sistema.
- Todo el equipo usa exactamente la misma versión (evita el "a mí me compila").
- Funciona igual en CI/CD.

Por eso ejecutas `.\mvnw.cmd compile` y no `mvn compile`.

**Comandos de Maven más usados:**

| Comando | Qué hace |
|---|---|
| `.\mvnw.cmd compile` | Compila el código fuente |
| `.\mvnw.cmd test` | Ejecuta los tests |
| `.\mvnw.cmd package` | Compila, testea y genera el `.jar` en `target/` |
| `.\mvnw.cmd clean` | Borra la carpeta `target/` |
| `.\mvnw.cmd clean package` | Limpia y reconstruye desde cero |
| `.\mvnw.cmd spring-boot:run` | Arranca la aplicación |
| `.\mvnw.cmd dependency:tree` | Muestra el árbol de dependencias (útil para conflictos) |

> En Linux/macOS sería `./mvnw` en lugar de `.\mvnw.cmd`.

---

## 4. Spring y Spring Boot

### Spring Framework

Un **framework** es un conjunto de librerías con una estructura definida:
resuelve problemas comunes para que no los programes desde cero, pero te impone
una forma de trabajar.

La idea central de Spring es la **inyección de dependencias** (DI), también
llamada **inversión de control** (IoC).

**Sin inyección de dependencias:**

```java
public class CandidaturaService {
    private CandidaturaRepository repo = new CandidaturaRepositoryImpl();
    // La clase decide qué implementación usar → acoplamiento fuerte
}
```

**Con inyección de dependencias:**

```java
public class CandidaturaService {
    private final CandidaturaRepository repo;

    public CandidaturaService(CandidaturaRepository repo) {
        this.repo = repo;  // alguien externo me da la dependencia
    }
}
```

*Por qué es mejor:* la clase no sabe ni le importa qué implementación concreta
recibe. Eso permite cambiarla sin tocar el código, y —muy importante— permite
**pasarle un objeto falso en los tests** (ahí entra Mockito más adelante).

Spring mantiene un **contenedor de IoC** que crea los objetos (llamados
**beans**), gestiona su ciclo de vida y los inyecta donde hacen falta.

> **Nota:** la inyección por constructor (como el ejemplo) es la forma
> recomendada, mejor que `@Autowired` sobre el campo. Permite marcar la
> dependencia como `final` y hace obvias las dependencias de la clase.

### Spring Boot

Spring Boot = Spring + **autoconfiguración** + **starters** + **servidor
embebido**.

- **Autoconfiguración**: mira qué librerías hay en el classpath y configura lo
  razonable por defecto. Si detecta un driver de base de datos, intenta
  configurar la conexión. Si detecta Spring Web, arranca un servidor.
- **Starters**: dependencias agrupadas por caso de uso. `spring-boot-starter-web`
  trae Spring MVC, Jackson (para JSON), Tomcat... todo compatible entre sí.
- **Servidor embebido**: ver sección siguiente.

**Ejemplo real vivido en este proyecto:** al añadir el driver de PostgreSQL,
Spring Boot intentó configurar automáticamente una conexión a base de datos y la
aplicación no arrancaba, porque esa base de datos no existía todavía. Solución
temporal:

```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
```

Esto le dice: "no autoconfigures la base de datos". **Es temporal** — se quita en
cuanto PostgreSQL esté funcionando.

### Spring Boot 4 y la modularización (importante)

Hasta Spring Boot 3.x, todas las clases de autoconfiguración vivían en un único
módulo gigante (`spring-boot-autoconfigure`, que llegó a pesar más de 2 MB).
Spring Boot 4 lo dividió en módulos pequeños y específicos, y cambió las rutas de
los paquetes.

| Clase | Spring Boot 3.x | Spring Boot 4.x |
|---|---|---|
| `DataSourceAutoConfiguration` | `org.springframework.boot.autoconfigure.jdbc` | `org.springframework.boot.jdbc.autoconfigure` |
| `HibernateJpaAutoConfiguration` | `org.springframework.boot.autoconfigure.orm.jpa` | `org.springframework.boot.hibernate.autoconfigure` |

**Consecuencia práctica:** la mayoría de tutoriales y respuestas de Stack
Overflow que encuentres están escritos para Spring Boot 3.x. Si un import no se
resuelve, sospecha de esto antes de pensar que el error es tuyo.

---

## 5. Tomcat embebido

Un **servidor web** es el programa que escucha peticiones HTTP en un puerto y las
entrega a tu aplicación.

**Forma tradicional (Java EE clásico):** instalabas Tomcat aparte, generabas un
archivo `.war` y lo "desplegabas" en el servidor. Configuración externa,
versiones que no coinciden, despliegues manuales.

**Forma de Spring Boot:** Tomcat va **dentro** de tu aplicación. El resultado es
un `.jar` ejecutable con `java -jar mi-app.jar`. La aplicación es autocontenida.

Por eso en el proyecto elegimos `Packaging: Jar` y no `War`, y por eso en los
logs de arranque aparece:

```
Tomcat started on port 8080 (http) with context path '/'
Started JobtrackApiApplication in 3.64 seconds
```

Esto también es lo que hace que dockerizar una app de Spring Boot sea trivial: no
hay que meter un servidor en la imagen.

---

## 6. API REST y JSON

### Qué es una API

*Application Programming Interface*: una interfaz para que **programas** se
comuniquen entre sí, en lugar de para que una persona haga clics.

Tu API no tiene interfaz visual. Devuelve datos. El frontend (React, en el
Proyecto 2) será quien los consuma y los muestre.

### REST

*Representational State Transfer*. Un estilo de diseño de APIs sobre HTTP con dos
ideas clave:

**1. Cada "cosa" es un recurso con su URL.** Los recursos se nombran con
sustantivos en plural, no con verbos:

- Bien: `/api/candidaturas`
- Mal: `/api/obtenerCandidaturas`

**2. El verbo HTTP dice qué haces con el recurso.**

| Verbo | URL | Acción | Idempotente |
|---|---|---|---|
| `GET` | `/api/candidaturas` | Listar todas | Sí |
| `GET` | `/api/candidaturas/5` | Obtener la nº 5 | Sí |
| `POST` | `/api/candidaturas` | Crear una nueva | No |
| `PUT` | `/api/candidaturas/5` | Reemplazar la nº 5 | Sí |
| `PATCH` | `/api/candidaturas/5` | Modificar parte de la nº 5 | No |
| `DELETE` | `/api/candidaturas/5` | Borrar la nº 5 | Sí |

> **Idempotente** significa que ejecutarlo varias veces produce el mismo
> resultado que ejecutarlo una vez. `DELETE` de la candidatura 5 diez veces deja
> el mismo estado final; `POST` diez veces crea diez candidaturas. Es una
> pregunta de entrevista bastante habitual.

### Códigos de estado HTTP

Tu API debe devolver el código correcto, no siempre 200:

| Código | Significado | Cuándo usarlo |
|---|---|---|
| `200 OK` | Todo bien | GET, PUT correctos |
| `201 Created` | Recurso creado | POST correcto |
| `204 No Content` | Correcto, sin cuerpo | DELETE correcto |
| `400 Bad Request` | Petición mal formada | Validación fallida |
| `401 Unauthorized` | No autenticado | Falta el token o es inválido |
| `403 Forbidden` | Autenticado pero sin permiso | Rol insuficiente |
| `404 Not Found` | No existe | ID que no está en la BD |
| `409 Conflict` | Conflicto de estado | Email ya registrado |
| `500 Internal Server Error` | Error del servidor | Excepción no controlada |

Devolver los códigos adecuados es una de las cosas que distingue una API
profesional de un ejercicio de clase.

### JSON

*JavaScript Object Notation*. El formato de intercambio de datos:

```json
{
  "id": 1,
  "empresa": "Indra",
  "puesto": "Junior Java Developer",
  "estado": "ENVIADA",
  "salario": 24000
}
```

Spring Boot usa **Jackson** para convertir automáticamente entre objetos Java y
JSON, en ambas direcciones. No tienes que hacerlo a mano.

Ese `[]` que viste en el navegador era un array JSON vacío: la lista sin
elementos que devolvía el controlador.

---

## 7. Anotaciones de Spring que ya has usado

Una **anotación** (`@algo`) es metadato: información sobre el código que otras
herramientas leen para tomar decisiones. No ejecuta nada por sí misma; Spring la
lee al arrancar y actúa en consecuencia.

```java
@RestController
@RequestMapping("/api/candidaturas")
public class CandidaturaController {

    @GetMapping
    public List<Object> listar() {
        return new ArrayList<>();
    }
}
```

| Anotación | Qué le dice a Spring |
|---|---|
| `@RestController` | Esta clase atiende peticiones HTTP y lo que devuelvan sus métodos se convierte a JSON automáticamente |
| `@RequestMapping("/api/candidaturas")` | URL base para todos los métodos de la clase |
| `@GetMapping` | Este método responde a `GET` en la URL base |
| `@SpringBootApplication` | Punto de entrada. Combina tres anotaciones: `@Configuration`, `@EnableAutoConfiguration` y `@ComponentScan` |

**Sobre `@RestController`:** es `@Controller` + `@ResponseBody`. La diferencia con
`@Controller` a secas es que este último se usa para devolver vistas HTML
(plantillas); `@RestController` devuelve datos.

**Sobre `@ComponentScan`** (dentro de `@SpringBootApplication`): Spring escanea el
paquete de la clase principal **y todos sus subpaquetes** buscando componentes.
Por eso `controller` debe estar dentro de `com.fabianlicea.jobtrack` — si lo
pusieras fuera, Spring no lo encontraría y el endpoint daría 404.

---

## 8. JPA, Hibernate y Spring Data JPA

### El problema

En Java trabajas con **objetos** (con herencia, referencias, colecciones). En una
base de datos relacional trabajas con **tablas, filas y claves foráneas**. Son
dos modelos distintos. Traducir entre ellos a mano significa escribir mucho SQL
repetitivo y mucho código de mapeo.

Esto se llama el *impedance mismatch* objeto-relacional.

### Las tres capas (no confundirlas)

Esta distinción sale en entrevistas:

| Nombre | Qué es |
|---|---|
| **JPA** | La *especificación*. Un estándar de Java que define cómo debe funcionar un ORM. Solo interfaces y anotaciones, no implementación. |
| **Hibernate** | Una *implementación* de JPA. La más usada. Es quien genera el SQL de verdad. |
| **Spring Data JPA** | Una *capa de abstracción* sobre JPA que reduce drásticamente el código de acceso a datos. |

Analogía: JPA es el enchufe estándar, Hibernate es el cargador que lo cumple,
Spring Data JPA es el adaptador que te lo pone todo más fácil.

### Qué es un ORM

*Object-Relational Mapping*. Traduce entre clases Java y tablas SQL:

- Una **clase** (`@Entity`) ↔ una **tabla**
- Un **objeto** (instancia) ↔ una **fila**
- Un **campo** ↔ una **columna**

### La magia de Spring Data JPA

Escribes una interfaz. Sin implementarla:

```java
public interface CandidaturaRepository extends JpaRepository<Candidatura, Long> {
    List<Candidatura> findByEmpresa(String empresa);
    List<Candidatura> findByEstadoOrderByFechaDesc(Estado estado);
}
```

Spring genera la implementación en tiempo de ejecución, leyendo el **nombre del
método** para deducir la consulta. `findByEmpresa` se convierte en
`SELECT * FROM candidaturas WHERE empresa = ?`.

Además, heredar de `JpaRepository` te da gratis: `save()`, `findById()`,
`findAll()`, `deleteById()`, `count()`, paginación y ordenación.

---

## 9. PostgreSQL

**Qué es.** Un sistema gestor de bases de datos relacionales (SGBD), gratuito y
open source. Los datos viven en tablas relacionadas entre sí, y se consultan con
SQL.

**Por qué esta y no otra.** En ofertas de empleo, PostgreSQL y MySQL son las dos
más pedidas. PostgreSQL tiene mejor reputación en entornos exigentes: cumple más
estrictamente el estándar SQL, soporta tipos avanzados (JSON, arrays,
geoespacial), y tiene mejor comportamiento en concurrencia.

**Relacional vs no relacional (NoSQL).** MongoDB y similares guardan documentos
sin esquema fijo. Son útiles para datos poco estructurados o de escala masiva.
Para una aplicación con entidades bien definidas y relaciones claras —como
JobTrack— una base relacional es la elección correcta, y saber justificar eso
también cuenta en una entrevista.

---

## 10. Docker

### El problema

"En mi ordenador funcionaba." Una aplicación depende de una versión de Java, de
librerías del sistema, de variables de entorno, de una base de datos concreta. Si
cualquiera de esas cosas difiere entre tu máquina y el servidor, algo se rompe.

### La solución

Un **contenedor** empaqueta la aplicación con todo lo que necesita para
funcionar. Corre igual en tu portátil, en el de un compañero y en producción.

### Contenedor vs máquina virtual

Una máquina virtual virtualiza un sistema operativo completo (con su propio
kernel): pesada, arranca en minutos, consume GBs. Un contenedor **comparte el
kernel** del sistema anfitrión y solo aísla los procesos: ligero, arranca en
segundos, consume MBs.

Esa comparación es una pregunta de entrevista muy frecuente.

### Conceptos

| Término | Qué es |
|---|---|
| **Imagen** | La plantilla inmutable (como una clase) |
| **Contenedor** | Una instancia en ejecución de una imagen (como un objeto) |
| **Dockerfile** | Las instrucciones para construir una imagen |
| **docker-compose.yml** | Define varios contenedores que trabajan juntos (ej: API + base de datos) |
| **Volumen** | Almacenamiento persistente. Sin él, al borrar el contenedor pierdes los datos |
| **Docker Hub** | Repositorio público de imágenes |

### Por qué en Windows necesita WSL2

Docker es tecnología nativa de Linux (usa características del kernel de Linux
para aislar procesos). En Windows, Docker Desktop usa **WSL2** (*Windows
Subsystem for Linux 2*), que proporciona un kernel de Linux real y ligero. De ahí
que haya que instalar WSL2 **antes** de Docker.

### Comandos de Docker más usados

| Comando | Qué hace |
|---|---|
| `docker --version` | Comprueba la instalación |
| `docker ps` | Lista contenedores en ejecución |
| `docker ps -a` | Lista todos, incluidos los parados |
| `docker images` | Lista imágenes descargadas |
| `docker logs <nombre>` | Muestra los logs de un contenedor |
| `docker exec -it <nombre> bash` | Abre una terminal dentro del contenedor |
| `docker stop <nombre>` | Para un contenedor |
| `docker rm <nombre>` | Borra un contenedor parado |
| `docker compose up -d` | Levanta los servicios del `docker-compose.yml` en segundo plano |
| `docker compose down` | Para y elimina esos servicios |
| `docker compose logs -f` | Sigue los logs en tiempo real |

---

## 11. Git y GitHub

**No son lo mismo**, y confundirlos en una entrevista queda mal:

- **Git** es el sistema de control de versiones. Un programa que corre en **tu
  ordenador** y guarda el historial completo de cambios de tu proyecto.
- **GitHub** es un servicio en la nube que hospeda repositorios Git y añade
  colaboración (pull requests, issues, CI/CD con Actions). Alternativas: GitLab,
  Bitbucket.

Git funciona perfectamente sin GitHub. GitHub no existiría sin Git.

### Las tres zonas de Git (clave para entender los comandos)

Este es el modelo mental que hace que todo lo demás encaje:

```
  Working Directory  →  Staging Area  →  Repository  →  Remote
  (tus archivos)        (git add)        (git commit)   (git push)
```

1. **Working Directory** — los archivos como están ahora en tu carpeta.
2. **Staging Area** (o *index*) — la zona intermedia donde marcas qué cambios
   quieres incluir en el próximo commit. Existe para poder hacer commits
   selectivos: si has tocado cinco archivos pero solo tres son de una misma
   tarea, puedes commitear solo esos tres.
3. **Repository** (local) — el historial de commits en tu máquina, en la carpeta
   oculta `.git`.
4. **Remote** — la copia en GitHub.

Cada comando mueve cambios de una zona a la siguiente. Si entiendes esto, dejas
de memorizar comandos.

### Qué es un commit

Una **foto del estado del proyecto** en un momento dado, con un identificador
único (hash), autor, fecha y mensaje. El historial es una cadena de commits.

**Buenos mensajes de commit.** La convención *Conventional Commits* es muy usada
en empresas:

```
feat: añadir endpoint de búsqueda de candidaturas
fix: corregir NullPointerException al filtrar por estado
refactor: extraer lógica de validación a un servicio
docs: actualizar README con instrucciones de Docker
test: añadir tests unitarios de CandidaturaService
chore: actualizar versión de Spring Boot
```

Un historial así impresiona a quien revise tu repositorio. Un historial de
`asdf`, `cambios`, `cambios2`, `ya va` no.

---

## 12. Comandos de Git — chuleta completa

### Configuración inicial (una vez por ordenador)

```bash
git config --global user.name "Tu Nombre"
git config --global user.email "tu@email.com"
git config --global init.defaultBranch main
git config --list                      # ver la configuración actual
```

### Empezar un repositorio

```bash
git init                               # crear repositorio en la carpeta actual
git clone <url>                        # descargar un repositorio existente
```

### El ciclo de trabajo diario

```bash
git status                             # ¿qué he cambiado? (úsalo constantemente)
git add archivo.java                   # añadir un archivo al staging
git add .                              # añadir todos los cambios
git add src/                           # añadir una carpeta
git commit -m "feat: descripción"      # guardar los cambios staged
git commit -am "mensaje"               # add + commit (solo archivos ya trackeados)
git push                               # subir al remoto
git pull                               # traer y fusionar cambios del remoto
```

### Ver qué ha pasado

```bash
git log                                # historial completo
git log --oneline                      # una línea por commit
git log --oneline --graph --all         # historial visual con ramas
git log -p archivo.java                # historial de un archivo con cambios
git diff                               # cambios sin stagear
git diff --staged                      # cambios ya en staging
git show <hash>                        # ver un commit concreto
git blame archivo.java                 # quién cambió cada línea
```

### Gestionar remotos

```bash
git remote -v                          # ver remotos configurados
git remote add origin <url>            # añadir un remoto
git remote remove origin               # quitarlo
git remote set-url origin <url>        # cambiar la URL
git push -u origin main                # primer push (establece el seguimiento)
```

> El `-u` (o `--set-upstream`) vincula tu rama local con la remota. Solo hace
> falta la primera vez; después basta con `git push`.

### Ramas (branches)

```bash
git branch                             # listar ramas locales
git branch -a                          # incluir remotas
git branch nombre-rama                 # crear rama
git switch nombre-rama                 # cambiar de rama
git switch -c nombre-rama              # crear y cambiar en un paso
git merge nombre-rama                  # fusionar una rama en la actual
git branch -d nombre-rama              # borrar rama ya fusionada
git branch -D nombre-rama              # forzar borrado
git branch -M main                     # renombrar la rama actual a main
```

> `git switch` y `git restore` son los comandos modernos. `git checkout` hacía
> ambas cosas (cambiar de rama y descartar cambios), lo cual era confuso.
> Verás `checkout` en tutoriales antiguos; funciona igual.

### Deshacer cosas

```bash
git restore archivo.java               # descartar cambios no staged
git restore --staged archivo.java      # quitar del staging (mantiene los cambios)
git commit --amend -m "nuevo mensaje"  # corregir el último commit
git reset --soft HEAD~1                # deshacer último commit, mantener cambios staged
git reset --mixed HEAD~1               # deshacer commit, cambios sin stagear
git reset --hard HEAD~1                # deshacer commit y BORRAR los cambios
git revert <hash>                      # crear un commit que deshace otro
```

> **Cuidado con `--hard`:** borra trabajo de forma irrecuperable.
>
> **Regla importante:** `reset` reescribe el historial, así que **nunca lo uses
> en commits que ya has subido** a un repositorio compartido. Para deshacer algo
> ya publicado, usa `revert`, que añade un commit nuevo en lugar de reescribir.

### Guardar trabajo temporalmente

```bash
git stash                              # guardar cambios y limpiar el directorio
git stash list                         # ver lo guardado
git stash pop                          # recuperar y eliminar del stash
git stash apply                        # recuperar sin eliminar
git stash drop                         # descartar
```

Útil cuando estás a mitad de algo y necesitas cambiar de rama urgentemente.

### Etiquetas (versiones)

```bash
git tag v1.0.0
git tag -a v1.0.0 -m "Primera versión"  # tag con anotación
git push origin v1.0.0                  # subir el tag
git push --tags                         # subir todos
```

### El `.gitignore`

Archivo que lista lo que Git debe ignorar. Fundamental. En un proyecto Java:

```gitignore
target/
*.class
.env
application-local.properties
.idea/
.vscode/
*.log
```

**Regla de oro:** nunca subas contraseñas, claves de API, ni archivos de
configuración con credenciales. Si subes una clave a un repositorio público,
considérala comprometida — borrarla en un commit posterior no la elimina del
historial.

---

## 13. De GitHub Desktop a la línea de comandos

Si vienes de GitHub Desktop, aquí está la traducción directa:

| En GitHub Desktop | En terminal |
|---|---|
| Ver la lista de "Changes" | `git status` |
| Marcar checkboxes de archivos | `git add <archivo>` |
| Escribir mensaje + "Commit to main" | `git commit -m "mensaje"` |
| Botón "Push origin" | `git push` |
| Botón "Fetch origin" / "Pull origin" | `git pull` |
| "Current Branch" → seleccionar otra | `git switch <rama>` |
| "New Branch" | `git switch -c <rama>` |
| "Branch → Merge into current branch" | `git merge <rama>` |
| Pestaña "History" | `git log --oneline` |
| Ver el diff de un archivo | `git diff <archivo>` |
| "Discard changes" (clic derecho) | `git restore <archivo>` |
| "Undo" del último commit | `git reset --soft HEAD~1` |
| "Stash changes" | `git stash` |
| "Add local repository" | `git init` (si es nuevo) |
| "Clone repository" | `git clone <url>` |
| "Repository → Repository settings → Remote" | `git remote set-url origin <url>` |

### ¿Por qué molestarse en aprender los comandos?

Razones prácticas, no de purismo:

1. **En el trabajo no siempre tendrás interfaz.** Conectado por SSH a un
   servidor, en un contenedor, en un pipeline de CI/CD: solo terminal.
2. **Las entrevistas técnicas preguntan por comandos**, no por botones.
3. **Entiendes lo que pasa.** GitHub Desktop oculta el modelo de las tres zonas,
   y cuando algo se rompe (un conflicto, un rebase a medias) no sabes qué está
   ocurriendo.
4. **Hay operaciones que la interfaz no expone**: `cherry-pick`, `rebase -i`,
   `bisect`, `reflog`.

**No pasa nada por usar las dos cosas.** Mucha gente con experiencia usa la
interfaz de VS Code para revisar diffs (es más cómoda visualmente) y la terminal
para todo lo demás. Lo importante es entender qué hace cada comando.

### Los 8 comandos que cubren el 90% del día

```bash
git status
git add .
git commit -m "mensaje"
git push
git pull
git log --oneline
git switch -c nueva-rama
git diff
```

---

## 14. Tecnologías que llegarán más adelante

### Bean Validation (`spring-boot-starter-validation`)

Validar datos de entrada con anotaciones en lugar de `if` encadenados:

```java
public class CandidaturaRequest {
    @NotBlank(message = "La empresa es obligatoria")
    private String empresa;

    @Positive
    private Integer salario;

    @Email
    private String contacto;
}
```

Con `@Valid` en el controlador, Spring valida automáticamente y devuelve `400`
si algo falla.

### DTOs (Data Transfer Objects)

Clases cuya única función es transportar datos entre capas.

**Por qué no devolver directamente las entidades JPA** (pregunta clásica de
entrevista, ten la respuesta preparada):

1. **Seguridad.** Una entidad `Usuario` tiene el campo `password`. Si la
   devuelves tal cual, expones el hash en el JSON.
2. **Acoplamiento.** Si la entidad es tu API, cualquier cambio en la base de
   datos rompe a los clientes. El DTO desacopla el modelo interno del contrato
   público.
3. **Serialización infinita.** Con relaciones bidireccionales (`Candidatura` →
   `Usuario` → lista de `Candidatura`...), Jackson entra en bucle infinito.
4. **Cargas perezosas.** Serializar una entidad con relaciones `LAZY` fuera de
   una transacción provoca `LazyInitializationException`.
5. **Control de la forma.** Puedes combinar datos de varias entidades o exponer
   campos calculados.

### Manejo global de errores

`@RestControllerAdvice` + `@ExceptionHandler` para centralizar el tratamiento de
excepciones y devolver respuestas de error consistentes, en lugar de dejar que
Spring devuelva un stacktrace.

### Spring Security + JWT

- **Spring Security**: el framework de autenticación (¿quién eres?) y
  autorización (¿qué puedes hacer?).
- **JWT** (*JSON Web Token*): un token firmado criptográficamente que el cliente
  envía en cada petición (`Authorization: Bearer <token>`). Contiene la identidad
  del usuario y sus roles.
- **Por qué JWT y no sesiones**: es *stateless*. El servidor no guarda sesiones,
  solo verifica la firma. Eso permite escalar horizontalmente (varias instancias
  sin sesiones compartidas) y sirve bien a clientes móviles o SPAs.
- **Hashing de contraseñas**: nunca se guardan en claro. Se usa **BCrypt**, que
  es lento a propósito para dificultar ataques por fuerza bruta.

### JUnit 5 + Mockito

- **JUnit 5**: el framework de tests en Java. Un test es un método anotado con
  `@Test` que comprueba un comportamiento con aserciones.
- **Mockito**: crea objetos falsos (*mocks*) para aislar lo que estás probando.
  Para testear `CandidaturaService` no quieres una base de datos real: le pasas
  un repositorio simulado que devuelve lo que tú decidas.
- **Tipos de test**: *unitarios* (una clase aislada, rápidos), *de integración*
  (varias capas juntas, con `@SpringBootTest`), *de endpoint* (con
  `MockMvc`, simulando peticiones HTTP).

Tener tests en el portfolio te diferencia mucho: casi ningún proyecto junior los
tiene.

### Swagger / OpenAPI

**OpenAPI** es la especificación para describir una API REST.
**Swagger UI** genera, a partir de esa especificación, una página web
interactiva donde se ven todos los endpoints y se pueden probar desde el
navegador.

Con la librería `springdoc-openapi` se genera automáticamente leyendo tus
controladores. Muy vistoso para un portfolio: quien revise tu proyecto puede
probar la API sin instalar nada.

### GitHub Actions (CI/CD)

- **CI** (*Integración Continua*): en cada push, un servidor compila el proyecto
  y ejecuta los tests. Si algo falla, te avisa.
- **CD** (*Despliegue Continuo*): si todo pasa, despliega automáticamente.

Se configura con un archivo YAML en `.github/workflows/`. El resultado visible es
el badge verde de "build passing" en el README — señal clara de que sabes lo que
haces.

---

## 15. Preguntas de entrevista y cómo responderlas

Preguntas reales y frecuentes sobre este stack. Prepara tus propias respuestas
con ejemplos de tu proyecto.

**Sobre Spring**
- ¿Qué es la inyección de dependencias y qué ventaja tiene?
- Diferencia entre `@Controller` y `@RestController`.
- ¿Qué hace `@SpringBootApplication`?
- ¿Qué es la autoconfiguración de Spring Boot?
- ¿Por qué inyección por constructor en lugar de `@Autowired` en el campo?
- Diferencia entre `@Component`, `@Service` y `@Repository`.

**Sobre REST**
- ¿Diferencia entre `PUT` y `PATCH`?
- ¿Qué significa que un método HTTP sea idempotente?
- ¿Qué código devuelves al crear un recurso? ¿Y al borrarlo?
- ¿Qué diferencia hay entre 401 y 403?

**Sobre JPA**
- Diferencia entre JPA, Hibernate y Spring Data JPA.
- ¿Por qué usar DTOs en lugar de devolver entidades? *(la clave)*
- ¿Qué es el problema N+1 y cómo se soluciona?
- Diferencia entre carga `EAGER` y `LAZY`.
- ¿Qué hace `@Transactional`?

**Sobre Docker**
- Diferencia entre imagen y contenedor.
- Diferencia entre contenedor y máquina virtual.
- ¿Para qué sirve un volumen?

**Sobre Git**
- Diferencia entre `merge` y `rebase`.
- Diferencia entre `reset` y `revert`, y cuándo usar cada uno.
- ¿Cómo resuelves un conflicto de merge?
- ¿Qué es un pull request y para qué sirve?

**Sobre el proyecto (las más importantes)**
- Explícame la arquitectura de tu proyecto.
- ¿Por qué elegiste estas tecnologías?
- ¿Cuál fue la parte más difícil y cómo la resolviste?
- Si tuvieras que añadir una funcionalidad X, ¿por dónde empezarías?
- ¿Qué mejorarías si tuvieras más tiempo?

> Las últimas cinco son las que más peso tienen. Un junior que sabe explicar sus
> propias decisiones vale más que uno que recita definiciones.

---

## 16. Registro de decisiones del proyecto

Decisiones tomadas y su justificación. Esto es material directo para la
entrevista.

| Decisión | Por qué |
|---|---|
| Compilar para Java 21 teniendo JDK 25 | Java 21 es lo más demandado en ofertas; un JDK superior puede compilar para versiones anteriores |
| Maven en lugar de Gradle | Más extendido en empresas con Java/Spring, especialmente en banca y consultoría |
| Packaging `Jar` en lugar de `War` | Spring Boot incluye Tomcat embebido; la app es autocontenida y se ejecuta con `java -jar` |
| Paquetes en minúsculas sin guiones | Convención estricta de Java; los guiones son sintácticamente inválidos en nombres de paquete |
| Empezar con solo 5 dependencias | Entender qué aporta cada una en vez de arrastrar configuración que no se comprende |
| `exclude` de `DataSourceAutoConfiguration` | **Temporal.** La autoconfiguración intentaba conectar a una BD inexistente. Se elimina al configurar PostgreSQL |
| PostgreSQL con Docker en lugar de instalación nativa | Docker aparece en las ofertas, no ensucia el sistema, y adelanta trabajo de la fase de contenedores |
| PostgreSQL en lugar de MySQL | Mejor cumplimiento del estándar SQL, tipos avanzados, mejor concurrencia |
| Estructura por capas (controller/service/repository) | Separación de responsabilidades; es el patrón estándar que se espera en Spring |

---

## Estado del proyecto

**Completado**
- [x] Entorno: JDK 25, Git, VS Code con extensiones de Java y Spring
- [x] Proyecto generado con Spring Initializr (Spring Boot 4.1.0, Java 21)
- [x] Repositorio Git local y remoto en GitHub
- [x] Compilación correcta (`BUILD SUCCESS`)
- [x] Aplicación arrancando con Tomcat embebido en el puerto 8080
- [x] Primer endpoint: `GET /api/candidaturas` devolviendo `[]`

**Siguiente**
- [ ] Instalar Docker Desktop (WSL2 primero)
- [ ] Levantar PostgreSQL en un contenedor
- [ ] Diseñar el modelo de datos (entidades y relaciones)
- [ ] Crear la entidad `Candidatura` con JPA
- [ ] Repositorio con Spring Data JPA
- [ ] CRUD completo
- [ ] Validaciones y DTOs
- [ ] Manejo global de errores
- [ ] Tests con JUnit y Mockito
- [ ] Spring Security + JWT
- [ ] Documentación con Swagger
- [ ] Dockerizar la aplicación
- [ ] README profesional
