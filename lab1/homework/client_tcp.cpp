#include <iostream>
#include <cstdlib>
#include <cstdio>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <signal.h>

#define TCP 1
#define UDP 2

#define JOIN_RING_MESSAGE 1
#define TOKEN_MESSAGE 2
#define ACCEPT_MESSAGE 3
#define RESPONSE_MESSAGE 4

#define MAX_ID_SIZE 40
#define MAX_MESSAGE_SIZE 200

using namespace std;


int logging_socket_fd;
struct sockaddr_in logger_address;


// typedef struct logging_message {

//     int xd;

// } logging_message;

typedef struct token {
    int type;
    int occupied;
    char sender_id[MAX_ID_SIZE];
    char receiver_id[MAX_ID_SIZE];
    char message[MAX_MESSAGE_SIZE];
    in_addr_t ip;
    uint16_t port;
} token;

char client_id[MAX_ID_SIZE];
in_addr_t listening_ip;
uint16_t listening_port;
in_addr_t neighbour_ip;
uint16_t neighbour_port;
int has_token = 0;
int protocol = TCP;

int listening_socket_fd;

int data_to_send = 0;
char receiver_of_a_message[MAX_ID_SIZE];
char message_content[MAX_MESSAGE_SIZE];


void rewrite_string_to_char_table(string from, char * to, int max_size) {
    for(int i = 0; i < max_size; i++) {
        to[i] = from[i];
        if(from[i] == '\0')
            break;
    }
}

int connect_to_neighbour() {
    // utworzenie socketa do polaczenia z sasiadem
    int neighbour_conection_socket_fd = socket(AF_INET, SOCK_STREAM, 0);
    if(neighbour_conection_socket_fd == -1) {
        perror("blad przy tworzeniu socketu");
        exit(1);
    }

    // wypelnienie adresu sasiada
    struct sockaddr_in neighbour_address;
    neighbour_address.sin_family = AF_INET;
    neighbour_address.sin_addr.s_addr = neighbour_ip;
    neighbour_address.sin_port = neighbour_port;

    // polaczenie z sasiadem
    if(connect(neighbour_conection_socket_fd, (struct sockaddr *) &neighbour_address, sizeof(neighbour_address)) != 0) {
        perror("blad przy laczeniu sie do serwera");
        exit(1);
    }

    return neighbour_conection_socket_fd;
}

void close_connection_with_neighbour(int neighbour_conection_socket_fd) {
    if(close(neighbour_conection_socket_fd) == -1) {
        perror("blad przy zamykaniu socketu polaczenia z sasiadem");
        exit(1);
    }
    // printf("zamknalem polaczenie z sasiadem\n");
}

void join_the_ring() {
    int neighbour_conection_socket_fd = connect_to_neighbour();

    // utworzenie wiadomosci oznajmujacej dolaczenie do ringu
    token * join_message = (token *) malloc(sizeof(token));
    join_message -> type = JOIN_RING_MESSAGE;
    join_message -> ip = listening_ip;
    join_message -> port = listening_port;

    // wyslanie wiadomosci
    if(write(neighbour_conection_socket_fd, join_message, sizeof(token)) == -1) {
        perror("blad przy wysylaniu wiadomosci");
        exit(1);
    }    

    // printf("wyslano wiadomosc do nastepnego nodea\n");

    // odebranie odpowiedzi zwrotnej
    token * response = (token *) malloc(sizeof(token));
    if(read(neighbour_conection_socket_fd, response, sizeof(token)) == -1) {
        perror("blad przy czytaniu wiadomosci");
        exit(1);
    }

    neighbour_ip = response -> ip;
    neighbour_port = response -> port;

    // printf("dodano do ringa\n");

    // zamkniecie polaczenia z sasiadem
    close_connection_with_neighbour(neighbour_conection_socket_fd);
}

void pass_token(token * message) {
    int neighbour_conection_socket_fd = connect_to_neighbour();

    if(write(neighbour_conection_socket_fd, message, sizeof(token)) == -1) {
        perror("blad przy wysylaniu wiadomosci");
        exit(1);
    }    
    
    // printf("przekazano wiadomosc do nastepnego nodea\n");
    
    close_connection_with_neighbour(neighbour_conection_socket_fd);
}

void send_accept_token(string receiver_id) {
    int neighbour_conection_socket_fd = connect_to_neighbour();

    // utworzenie wiadomosci akceptujacej
    token * accept_message = (token *) malloc(sizeof(token));
    accept_message -> type = ACCEPT_MESSAGE;
    rewrite_string_to_char_table(receiver_id, accept_message -> receiver_id, MAX_ID_SIZE);

    if(write(neighbour_conection_socket_fd, accept_message, sizeof(token)) == -1) {
        perror("blad przy wysylaniu wiadomosci");
        exit(1);
    }    

    // printf("wyslano wiadomosc akceptujaca\n");
    
    close_connection_with_neighbour(neighbour_conection_socket_fd);
}

void send_new_token_without_data() {
    int neighbour_conection_socket_fd = connect_to_neighbour();

    token * empty_message = (token *) malloc(sizeof(token));
    empty_message -> type = TOKEN_MESSAGE;
    empty_message -> occupied = 0;

    if(write(neighbour_conection_socket_fd, empty_message, sizeof(token)) == -1) {
        perror("blad przy wysylaniu wiadomosci");
        exit(1);
    }    

    // printf("wyslano pusta wiadomosc\n");
    
    close_connection_with_neighbour(neighbour_conection_socket_fd);
}

void send_new_token_with_data() {
    int neighbour_conection_socket_fd = connect_to_neighbour();

    token * new_message = (token *) malloc(sizeof(token));
    new_message -> type = TOKEN_MESSAGE;
    new_message -> occupied = 1;
    rewrite_string_to_char_table(message_content, new_message -> message, MAX_MESSAGE_SIZE);
    rewrite_string_to_char_table(receiver_of_a_message, new_message -> receiver_id, MAX_ID_SIZE);
    rewrite_string_to_char_table(client_id, new_message -> sender_id, MAX_ID_SIZE);

    if(write(neighbour_conection_socket_fd, new_message, sizeof(token)) == -1) {
        perror("blad przy wysylaniu wiadomosci");
        exit(1);
    }    

    // printf("wyslano nowa wiadomosc z trescia\n");
    
    close_connection_with_neighbour(neighbour_conection_socket_fd);

    data_to_send = 0;
}

void connect_with_new_client(int connection_fd, token * message) {
    // utworzenie odpowiedzi zawierajacej ip i port na ktory dotychczas sie wysylalo
    token * response = (token *) malloc(sizeof(token));
    response -> type = RESPONSE_MESSAGE;
    response -> ip = neighbour_ip;
    response -> port = neighbour_port;

    // zmiana ip i portu na ktory wysylamy na adres nowego klienta
    neighbour_ip = message -> ip;
    neighbour_port = message -> port;

    if(write(connection_fd, response, sizeof(token)) == -1) {
        perror("blad przy wysylaniu wiadomosci");
        exit(1);
    }            

    // jesli bylismy pierwszym klientem, to czekamy chwile i wysylamy token
    if(has_token == 1) {
        has_token = 2;
        sleep(2);
        send_new_token_without_data();
    }
}


void handle_new_connection(int connection_fd) {
    // printf("mam nowe polaczenie\n");

    token * message = (token *) malloc(sizeof(token));
    if(read(connection_fd, message, sizeof(token)) == -1) {
        perror("blad przy czytaniu wiadomosci");
        exit(1);
    }
    
    // printf("typ wiadomosci z polaczenia - %d", message -> type);

    if(message -> type == JOIN_RING_MESSAGE) {
        // printf("otrzymano polaczenie od nowego klienta\n")
        connect_with_new_client(connection_fd, message);
    }
    if(message -> type == TOKEN_MESSAGE) {
        // przetrzymanie tokena przez sekunde
        sleep(1);

        if(message -> occupied == 0) {
            // jesli token byl pusty to wysylamy dane jesli takie mamy lub pusty token
            if(data_to_send == 1) 
                send_new_token_with_data();
            else 
                send_new_token_without_data();
        }
        else {
            // jesli token byl pelny to sprawdzamy czy wiadomosc jest do nas, lub od nas, jesli nie przekazujemy dalej
            if(strcmp(message -> receiver_id, client_id) == 0) {
                printf("    OTRZYMALEM WIADOMOSC:\n\tod: %s\n\ttresc: %s\n", message -> sender_id, message -> message);                
                send_accept_token(message -> sender_id);
            }
            else if(strcmp(message -> sender_id, client_id) == 0) {
                printf("    NIE UDALO SIE DOSTARCZYC WIADOMOSCI DO: %s (%s)\n", message -> receiver_id, message -> message);
                send_new_token_without_data();
            }
            else 
                pass_token(message);
        }
    }
    if(message -> type == ACCEPT_MESSAGE) {
        // jesli dostaniemy wiadomosc akceptujaca do nas to przekazujemy pusty token - zapobiega glodzeniu
        if(strcmp(message -> receiver_id, client_id) == 0)
            send_new_token_without_data();
        else
            pass_token(message);
    }

}

void listen_for_connection() {
    // utworzenie socketa do nasluchiwania
    listening_socket_fd = socket(AF_INET, SOCK_STREAM, 0);
    if(listening_socket_fd == -1) {
        perror("blad przy tworzeniu socketu");
        exit(1);
    }

    // printf("utworzono socket do nasluchiwania\n");

    // wypelnienie adresu do nasluchiwania
    struct sockaddr_in listening_address;
    listening_address.sin_family = AF_INET;
    listening_address.sin_addr.s_addr = htonl(INADDR_ANY);
    listening_address.sin_port = listening_port;

    // bindowanie socketa do adresu
    if(bind(listening_socket_fd, (struct sockaddr *) &listening_address, sizeof(listening_address)) != 0) {
        perror("blad przy bindowaniu socketu do adresu");
        exit(1);
    }

    // printf("zbindowano socket do nasluchiwania\n");

    while(1) {
        // nasluchiwanie na polaczenie w nieskonczonej petli
        if(listen(listening_socket_fd, 5) != 0) {
            perror("blad przy nasluchiwaniu na polaczenie");
            exit(1);
        }

        // zaakceptowanie polaczenia
        struct sockaddr_in new_connection_address;
        socklen_t address_length = sizeof(new_connection_address);
        int connection_fd = accept(listening_socket_fd, (struct sockaddr *)  &new_connection_address, &address_length);
        if(connection_fd == -1) {
            perror("blad przy akceptowaniu polaczenia");
            exit(1);
        }

        handle_new_connection(connection_fd);

        // zamkniecie obsluzonego polaczenia
        if(close(connection_fd) == -1) {
            perror("blad przy zamykaniu socketu");
            exit(1);
        }

        // printf("zamknalem polaczenie przychodzace, wracam do sluchania\n");
    }
}


void * chat_thread_fun(void * arg){
    sleep(3);
    while(1) {
        printf("------------------Mozesz stworzyc wiadomosc------------------\n");
        
        int n = 0;
        printf("wpisz identyfikator odbiorcy\n");
        while ((receiver_of_a_message[n++] = getchar()) != '\n');
        receiver_of_a_message[n-1] = '\0';

        n = 0;
        printf("wpisz tresc wiadomosci\n");
        while ((message_content[n++] = getchar()) != '\n');
        message_content[n-1] = '\0';

        // cout << "\tchcesz wyslac |" << receiver_of_a_message << "| message: |" << message_content << "|\n";
        printf("utworzyles wiadomosc, zaczekaj az wysle sie ta aby moc stworzyc kolejna\n");

        data_to_send = 1;

        while(data_to_send == 1){
            sleep(1);
        }
    }
}

void setup_logger() {
    logging_socket_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if(logging_socket_fd < 0 ) { 
        perror("Blad przy tworzeniu socketu do logowania"); 
        exit(1); 
    } 
    logger_address.sin_family = AF_INET;
    logger_address.sin_addr.s_addr = inet_addr("224.0.0.2");
    logger_address.sin_port = htons(22222);
}

void assign_arguments_to_variables(char ** argv) {
    // identyfikator klienta
    rewrite_string_to_char_table(argv[1], client_id, MAX_ID_SIZE);

    // ip na ktorym beda do nas wysylane wiadomosci
    listening_ip = inet_addr(argv[2]);
    if(listening_ip == INADDR_NONE) {
        printf("Zly format ip sasiada\n");
        exit(1);
    }

    // port na ktorym nasluchuje klient
    listening_port = htons(atoi(argv[3]));
    
    // ip sasiada
    neighbour_ip = inet_addr(argv[4]);
    if(neighbour_ip == INADDR_NONE) {
        printf("Zly format ip sasiada\n");
        exit(1);
    }

    // port sasiada
    neighbour_port = htons(atoi(argv[5]));

    //posiadanie tokenu
    if(strcmp("tak", argv[6]) == 0) {
        has_token = 1;
        neighbour_ip = listening_ip;
        neighbour_port = listening_port;
    }

    // wykorzystywany protokol
    if(strcmp("udp", argv[7]) == 0)
        protocol = UDP;

}

void sigint_fun(int signal_number){
    printf("\nOtrzymano sygnal SIGINT, zamykanie\n");
    exit(0);
}

void exit_fun() {
    if(close(listening_socket_fd) == -1) {
        perror("blad przy zamykaniu socketu do nasluchiwania");
    }
    // printf("zamknieto socket do nasluchiwania\n");
    if(close(logging_socket_fd) == -1) {
        perror("blad przy zamykaniu socketu do logowania");
    }
    // printf("zamknieto socket do logowania\n");
}

int main(int argc, char ** argv) {
    // sprawdzenie ilosci argumentow wejsciowych
    if(argc != 8) {
        printf("Zla liczba argumentow\nuzycie: ./client identyfikator ip_do_nasluchiwania port_do_nasluchiwania ip_sasiada port_sasiada token(tak/nie) tcp/udp\n");
        exit(1);
    }
    
    // przypisanie argumentow do zmiennych
    assign_arguments_to_variables(argv);

    // ustawienie funkcji wyjscia
    if(atexit(exit_fun) != 0){
        perror("Blad przy ustawianiu funckji wyjscia");
        exit(1);
    }

    // ustawienie obslugi sygnalu SIGINT
    struct sigaction sigact;
    sigact.sa_handler = sigint_fun;
    sigemptyset(&sigact.sa_mask);
    if(sigaction(SIGINT, &sigact, NULL) != 0){
        perror("Blad przy ustawieniu funkcji obslugi sygnalu SIGINT");
        exit(1);
    }

    // rozpoczecie watku odpowiedzialnego za chat
    pthread_t chat_thread_id;
    pthread_create(&chat_thread_id, NULL, chat_thread_fun, NULL);

    // przygotowanie socketu do logowania
    setup_logger();


    // loggin
    char * msg = (char *) malloc(50);
    rewrite_string_to_char_table("czesc czesc", msg, 50);
    sendto(logging_socket_fd, msg, 50, 0, (const struct sockaddr *) &logger_address, sizeof(logger_address));
    printf("log sent\n");


    // // dolaczenie do ringu
    // if(has_token == 0)
    //     join_the_ring();

    // // nasluchiwanie na nowe polaczenia
    // listen_for_connection();
}
