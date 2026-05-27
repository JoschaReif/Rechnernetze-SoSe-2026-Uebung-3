import java.io.IOException;
import java.util.Scanner;

public class Local_Test {

    public static void main(String[] args) throws IOException {

        String[] server = {"-l", "12345"};

        String[] client = {"127.0.0.1", "12345"};

        String[] listenTalk = {"-l", "12345"};

        String[] connectTalk = {"127.0.0.1", "54321"};

        Scanner sc = new Scanner(System.in);

        System.out.println("2 für UDP, 3 für TCP");

        int choose = sc.nextInt();

        if(choose == 3){
            System.out.println("Ist dies ein Server? j/n");
            String serv = sc.next();
            if ((serv.equals("j"))){
                netcat_TCP.main(listenTalk);
            } else if (serv.equals("n")) {
                netcat_TCP.main(connectTalk);
            }else {
                System.out.println("ungültige Eingabe");
            }
        } else if (choose == 2) {
            System.out.println("Ist dies ein Listener? j/n");
            String serv = sc.next();
            if ((serv.equals("j"))){
                netcat_UDP.main(server);
            } else if (serv.equals("n")) {
                netcat_UDP.main(client);
            }else {
                System.out.println("ungültige Eingabe");
            }
        }else {
            System.out.println("ungültige Eingabe");
        }

    }

}