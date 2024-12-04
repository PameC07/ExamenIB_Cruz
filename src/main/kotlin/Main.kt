import com.google.gson.* // Librerías para manejar JSON
import java.io.File
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Ciudad(
    val nombre: String,
    val poblacion: Long,
    val altitud: Double,
    val fechaFundacion: LocalDate,
    val esCapital: Boolean
)

data class Pais(
    val nombre: String,
    val codigoISO: String,
    val continente: String,
    val poblacion: Long,
    val esMiembroONU: Boolean,
    val ciudades: MutableList<Ciudad> = mutableListOf() // Lista mutable, REPRESENTA LA RELACION UNO A MUCHOS ENTRE PAIS Y CIUDAD
)

// Clase para adaptar LocalDate a JSON y viceversa usando Gson.
class LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Formato de fecha

    // Método para serializar LocalDate a JSON.
    override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(formatter)) // Convierte LocalDate a String en formato "yyyy-MM-dd"
    }

    // Método para deserializar JSON a LocalDate.
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDate {
        return LocalDate.parse(json?.asString, formatter) // Convierte la cadena JSON a LocalDate
    }
}

// Función para guardar los datos en un archivo JSON.
fun guardarDatos(paises: List<Pais>, archivo: File, gson: Gson) {
    archivo.writeText(gson.toJson(paises)) // Convierte la lista de países a formato JSON y la escribe en el archivo
}

fun main() {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val gson = GsonBuilder()
        .setPrettyPrinting() // Formato legible de JSON
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()

    // Cargar datos desde el archivo si existe; si no, se crea una lista vacía de países.
    val paisesFile = File("paises.json")
    val paises = if (paisesFile.exists()) {
        gson.fromJson(paisesFile.readText(), Array<Pais>::class.java)?.toMutableList() ?: mutableListOf() // Cargar los países del archivo
    } else {
        mutableListOf() // Lista vacía si no existe el archivo
    }

    while (true) {
        println("\nMenú Principal:")
        println("1. Crear País")
        println("2. Mostrar Países")
        println("3. Actualizar País")
        println("4. Eliminar País")
        println("5. Agregar Ciudad a un País")
        println("6. Salir")
        print("Selecciona una opción: ")

        when (readln().toIntOrNull()) {
            // Opción 1: Crear un nuevo país.
            1 -> {
                // Solicitar datos para crear un país.
                print("Nombre del país: ")
                val nombre = readln()
                print("Código del país: ")
                val codigo = readln()
                print("Continente: ")
                val continente = readln()
                print("Población total: ")
                val poblacion = readln().toLongOrNull() ?: 0L // Validación de entrada
                print("¿Es miembro de la ONU? (true/false): ")
                val esMiembroONU = readln().toBooleanStrictOrNull() ?: false // Validación de booleano
                // Agregar el país a la lista.
                paises.add(Pais(nombre, codigo, continente, poblacion, esMiembroONU))
                guardarDatos(paises, paisesFile, gson) // Guardar cambios en el archivo
                println("País agregado correctamente.")
            }

            // Opción 2: Mostrar la lista de países.
            2 -> {
                if (paises.isEmpty()) {
                    println("No hay países registrados.")
                } else {
                    // Mostrar cada país con su índice.
                    paises.forEachIndexed { index, pais ->
                        println("[$index] $pais")
                    }
                }
            }

            // Opción 3: Actualizar un país existente.
            3 -> {
                print("Índice del país a actualizar: ")
                val index = readln().toIntOrNull()
                if (index != null && index in paises.indices) {
                    val pais = paises[index]
                    // Solicitar los nuevos valores; si se dejan en blanco, se mantienen los actuales.
                    print("Nuevo nombre (${pais.nombre}): ")
                    val nuevoNombre = readln()
                    print("Nuevo continente (${pais.continente}): ")
                    val nuevoContinente = readln()
                    print("Nueva población (${pais.poblacion}): ")
                    val nuevaPoblacion = readln().toLongOrNull() ?: pais.poblacion
                    print("¿Es miembro de la ONU? (${pais.esMiembroONU}): ")
                    val nuevoMiembroONU = readln().toBooleanStrictOrNull() ?: pais.esMiembroONU
                    // Actualizar el país en la lista.
                    paises[index] = pais.copy(
                        nombre = nuevoNombre.ifBlank { pais.nombre },
                        continente = nuevoContinente.ifBlank { pais.continente },
                        poblacion = nuevaPoblacion,
                        esMiembroONU = nuevoMiembroONU
                    )
                    guardarDatos(paises, paisesFile, gson) // Guardar cambios en el archivo
                    println("País actualizado correctamente.")
                } else {
                    println("Índice inválido.")
                }
            }

            // Opción 4: Eliminar un país de la lista.
            4 -> {
                print("Índice del país a eliminar: ")
                val index = readln().toIntOrNull()
                if (index != null && index in paises.indices) {
                    paises.removeAt(index) // Eliminar país de la lista
                    guardarDatos(paises, paisesFile, gson) // Guardar cambios en el archivo
                    println("País eliminado correctamente.")
                } else {
                    println("Índice inválido.")
                }
            }

            // Opción 5: Agregar una ciudad a un país.
            5 -> {
                print("Índice del país al que agregar una ciudad: ")
                val index = readln().toIntOrNull()
                if (index != null && index in paises.indices) {
                    val pais = paises[index]
                    // Solicitar datos para la nueva ciudad.
                    print("Nombre de la ciudad: ")
                    val nombre = readln()
                    print("Población: ")
                    val poblacion = readln().toLongOrNull() ?: 0L
                    print("Altitud: ")
                    val altitud = readln().toDoubleOrNull() ?: 0.0
                    print("Fecha de fundación (yyyy-MM-dd): ")
                    val fechaFundacion = try {
                        LocalDate.parse(readln(), formatter) // Convertir entrada a LocalDate
                    } catch (e: Exception) {
                        LocalDate.now() // Valor predeterminado si hay error
                    }
                    print("¿Es capital? (true/false): ")
                    val esCapital = readln().toBooleanStrictOrNull() ?: false
                    // Agregar la ciudad al país seleccionado.
                    pais.ciudades.add(Ciudad(nombre, poblacion, altitud, fechaFundacion, esCapital))
                    guardarDatos(paises, paisesFile, gson) // Guardar cambios en el archivo
                    println("Ciudad agregada correctamente.")
                } else {
                    println("Índice inválido.")
                }
            }

            // Opción 6: Salir del programa.
            6 -> {
                println("Saliendo del programa...")
                break
            }

            // Manejo de entrada inválida.
            else -> println("Opción inválida. Intenta de nuevo.")
        }
    }
}
