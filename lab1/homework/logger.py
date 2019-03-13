import socket

ip = "224.0.0.2"
port = 2222

# stworzenie socketa udp
my_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
# pozwolenie wielu socketom korzystac z tego samego adresu
my_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
# bind socketu do adresu
my_socket.bind((ip, port))
# dodanie socketu do grupy multicastowej
my_socket.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, socket.inet_aton(ip) + socket.inet_aton("0.0.0.0"))

while 1:
    data, addr = my_socket.recvfrom(50)
    print("Od: " + str(addr) + " - Tresc: " + str(data.decode("utf-8")))


