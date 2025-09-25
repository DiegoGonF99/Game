import java.util.*;


class InvalidChoiceException extends Exception {
    public InvalidChoiceException(String msg) {
        super(msg);
    }
}

class AttackMissedException extends Exception {
    public AttackMissedException(String msg) {
        super(msg);
    }
}

// Enum para tipos de Pokémon/ataques
enum Tipo {
    FUEGO, AGUA, PLANTA, ELECTRICO, NORMAL
}

// Clase Ataque
class Ataque {
    String nombre;
    int precision;
    int danioBase;
    Tipo tipo;

    public Ataque(String nombre, int precision, int danioBase, Tipo tipo) {
        this.nombre = nombre;
        this.precision = precision;
        this.danioBase = danioBase;
        this.tipo = tipo;
    }

    public int ejecutar() throws AttackMissedException {
        Random r = new Random();
        if (r.nextInt(100) >= precision) {
            throw new AttackMissedException("El ataque " + nombre + " falló");
        }

        int danio = danioBase;
        switch (tipo) {
            case FUEGO -> danio += 5;
            case AGUA -> danio += 3;
            case PLANTA -> danio += 4;
            case ELECTRICO -> danio += 6;
            default -> {}
        }
        return danio;
    }

    public String toString() {
        return nombre + " (Daño: " + danioBase + ", Precisión: " + precision + "%)";
    }
}

// Clase abstracta Pokémon
abstract class Pokemon {
    String nombre;
    Tipo tipo;
    int hpMax;
    int hp;
    List<Ataque> ataquesDisponibles = new ArrayList<>();

    public Pokemon(String nombre, Tipo tipo, int hpMax) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.hpMax = hpMax;
        this.hp = hpMax;
    }

    public void recibirDanio(int cantidad) {
        hp -= cantidad;
        if (hp < 0) hp = 0;
    }

    public boolean estaVivo() {
        return hp > 0;
    }

    public String toString() {
        return nombre + " [" + tipo + "] HP: " + hp + "/" + hpMax;
    }
}

// Subclases
class Charmander extends Pokemon {
    public Charmander() {
        super("Charmander", Tipo.FUEGO, 50);
        ataquesDisponibles.add(new Ataque("Lanzallamas", 80, 15, Tipo.FUEGO));
        ataquesDisponibles.add(new Ataque("Arañazo", 95, 7, Tipo.NORMAL));
    }
}

class Squirtle extends Pokemon {
    public Squirtle() {
        super("Squirtle", Tipo.AGUA, 55);
        ataquesDisponibles.add(new Ataque("Burbuja", 90, 9, Tipo.AGUA));
        ataquesDisponibles.add(new Ataque("Placaje", 95, 6, Tipo.NORMAL));
    }
}

class Bulbasaur extends Pokemon {
    public Bulbasaur() {
        super("Bulbasaur", Tipo.PLANTA, 60);
        ataquesDisponibles.add(new Ataque("Latigazo", 85, 12, Tipo.PLANTA));
        ataquesDisponibles.add(new Ataque("Placaje", 95, 6, Tipo.NORMAL));
    }
}

class Pikachu extends Pokemon {
    public Pikachu() {
        super("Pikachu", Tipo.ELECTRICO, 45);
        ataquesDisponibles.add(new Ataque("Impactrueno", 85, 14, Tipo.ELECTRICO));
        ataquesDisponibles.add(new Ataque("Ataque rápido", 95, 8, Tipo.NORMAL));
    }
}

// Clase principal
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random r = new Random();

        Map<Integer, Pokemon> pokedex = Map.of(
                1, new Charmander(),
                2, new Squirtle(),
                3, new Bulbasaur(),
                4, new Pikachu()
        );

        System.out.print("Ingresa tu nombre: ");
        String nombreJugador = sc.nextLine();

        Pokemon jugador = null;
        while (jugador == null) {
            try {
                System.out.println("\nElige tu Pokémon:");
                pokedex.forEach((k, v) -> System.out.println(k + ". " + v.nombre));
                int eleccion = sc.nextInt();
                if (!pokedex.containsKey(eleccion)) {
                    throw new InvalidChoiceException("Opción inválida. Solo del 1 al 4.");
                }
                jugador = pokedex.get(eleccion);
            } catch (InputMismatchException e) {
                System.out.println("Debes escribir un número.");
                sc.nextLine();
            } catch (InvalidChoiceException e) {
                System.out.println(e.getMessage());
            }
        }

        // CPU
        Pokemon cpu;
        do {
            cpu = pokedex.get(r.nextInt(4) + 1);
        } while (cpu.nombre.equals(jugador.nombre));

        System.out.println("\n" + nombreJugador + " eligió a " + jugador.nombre);
        System.out.println("CPU eligió a " + cpu.nombre);

        List<Integer> danios = new ArrayList<>();
        List<String> registro = new ArrayList<>();

        while (jugador.estaVivo() && cpu.estaVivo()) {
            System.out.println("\nTu turno. Elige un ataque:");
            for (int i = 0; i < jugador.ataquesDisponibles.size(); i++) {
                System.out.println((i + 1) + ". " + jugador.ataquesDisponibles.get(i));
            }

            try {
                int op = sc.nextInt();
                if (op < 1 || op > jugador.ataquesDisponibles.size()) {
                    throw new InvalidChoiceException("Ese ataque no existe.");
                }
                Ataque atkJugador = jugador.ataquesDisponibles.get(op - 1);
                int d = atkJugador.ejecutar();
                cpu.recibirDanio(d);
                danios.add(d);
                String log = jugador.nombre + " usó " + atkJugador.nombre + " e hizo " + d + " de daño.";
                registro.add(log);
                System.out.println(log);
            } catch (InvalidChoiceException e) {
                System.out.println(e.getMessage());
            } catch (AttackMissedException e) {
                System.out.println(e.getMessage());
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida, perdiste tu turno.");
                sc.nextLine();
            }

            if (!cpu.estaVivo()) break;

            Ataque atkCpu = cpu.ataquesDisponibles.get(r.nextInt(cpu.ataquesDisponibles.size()));
            try {
                int dCpu = atkCpu.ejecutar();
                jugador.recibirDanio(dCpu);
                danios.add(dCpu);
                String log = cpu.nombre + " usó " + atkCpu.nombre + " e hizo " + dCpu + " de daño.";
                registro.add(log);
                System.out.println(log);
            } catch (AttackMissedException e) {
                System.out.println(cpu.nombre + " falló su ataque.");
            }

            System.out.println(jugador);
            System.out.println(cpu);
        }

        System.out.println("\n--- Resultado ---");
        if (jugador.estaVivo()) {
            System.out.println("Ganaste");
        } else {
            System.out.println("Perdiste contra " + cpu.nombre);
        }

        int total = 0, max = 0;
        for (int d : danios) {
            total += d;
            if (d > max) max = d;
        }
        double prom = danios.isEmpty() ? 0 : (double) total / danios.size();

        System.out.println("\nEstadísticas:");
        System.out.println("Daño total: " + total);
        System.out.println("Golpe más fuerte: " + max);
        System.out.printf("Promedio de daño: %.2f%n", prom);

        System.out.println("\nRegistro de la batalla:");
        registro.forEach(m -> System.out.println("- " + m));
    }
}
