import pika
import threading
import uuid
from normal_user import NormalUser


class Doctor(NormalUser):

    def __init__(self):

        super().__init__()

        self.sending_connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
        self.listening_connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))

        # stworzenie kanalow do wysylania i nasluchiwania
        self.sending_channel = self.sending_connection.channel()
        self.listening_channel = self.listening_connection.channel()

        # stworzenie exchange do wysylania requestow
        self.sending_channel.exchange_declare(exchange='doctor_exchange',
                                              exchange_type='topic',
                                              auto_delete=True)

        # stworzenie i zbindowanie kolejek na poszczegolne kontuzje
        for queue_name in ['hip', 'knee', 'elbow']:
            self.sending_channel.queue_declare(queue=queue_name,
                                               auto_delete=True)
            self.sending_channel.queue_bind(exchange='doctor_exchange',
                                            queue=queue_name,
                                            routing_key='injury.' + queue_name)

        # stworzenie kolejki na odpowiedzi od technikow
        self.listening_channel.queue_declare(queue='response',
                                             auto_delete=True)

        # subskrybcja na kolejce na odpowiedzi od technikow
        self.listening_channel.basic_qos(prefetch_count=1)
        self.listening_channel.basic_consume(queue='response',
                                             on_message_callback=self.on_response)

        # stworzenie zbioru correlation_id'ków wiadomosci ktore wyslal dany doktor
        self.correlation_ids = set()
        self.ids_set_lock = threading.Lock()

        # rozpoczecie nasluchiwania na kolejce odpowiedzi od technikow
        listening_thread = threading.Thread(target=self.listening_channel.start_consuming)
        listening_thread.start()

    def send_test_request(self, patient_name, injury_type):
        routing_key = 'injury.' + injury_type
        message = patient_name + ' ' + injury_type
        correlation_id = str(uuid.uuid4())
        with self.ids_set_lock:
            self.correlation_ids.add(correlation_id)
            self.sending_channel.basic_publish(exchange='doctor_exchange',
                                               routing_key=routing_key,
                                               body=message,
                                               properties=pika.BasicProperties(correlation_id=correlation_id))

            self.log_channel.basic_publish(exchange='log_exchange',
                                           routing_key='',
                                           body='Lekarz wysyla: ' + message)

    def on_response(self, ch, method, props, body):
        correlation_id = props.correlation_id

        with self.ids_set_lock:
            if correlation_id in self.correlation_ids:
                print('Otrzymalem odpowiedz DLA MNIE: ' + body.decode())
                self.correlation_ids.remove(correlation_id)
                ch.basic_ack(delivery_tag=method.delivery_tag)
            else:
                print('Otrzymalem odpowiedz nie dla mnie (' + body.decode() + ')')
                ch.basic_nack(delivery_tag=method.delivery_tag)


if __name__ == "__main__":

    doctor = Doctor()
    print("Uzycie: nazwa_pacjenta hip/knee/elbow")
    while True:
        input_words = input().split()
        if len(input_words) == 2 and input_words[1] in ('hip', 'knee', 'elbow'):
            doctor.send_test_request(input_words[0], input_words[1])
            print("Wyslano request: " + input_words[0] + " " + input_words[1])
        else:
            print("Bledny input")
