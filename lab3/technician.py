import pika
import threading
import uuid
from time import sleep
from normal_user import NormalUser


class Technician(NormalUser):

    def __init__(self, spec1, spec2):

        super().__init__()

        self.connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
        # stworzenie kanalu do odbierania i wysylania odpowiedzi
        self.channel = self.connection.channel()

        # stworzenie exchange do wysylania odpowiedzi
        self.channel.exchange_declare(exchange='technician_exchange',
                                      exchange_type='direct',
                                      auto_delete=True)

        # stworzenie i zbindowanie kolejki na odpowiedzi
        self.channel.queue_declare(queue='response',
                                   auto_delete=True)
        self.channel.queue_bind(exchange='technician_exchange',
                                queue='response',
                                routing_key='resp_msg')

        # stworzenie kolejek na poszczegolne specjalizacje
        for queue_name in [spec1, spec2]:
            self.channel.queue_declare(queue=queue_name,
                                       auto_delete=True)

        # subskrybcja na kolejkach odpowiednich specjalizacji
        self.channel.basic_qos(prefetch_count=1)
        self.channel.basic_consume(queue=spec1,
                                   on_message_callback=self.perform_test)
        self.channel.basic_consume(queue=spec2,
                                   on_message_callback=self.perform_test)

        print("Utworzono technika")
        # rozpoczecie nasluchiwania
        self.channel.start_consuming()

    def perform_test(self, ch, method, props, body):
        print('Otrzymalem zlecenie: ' + body.decode() + ' (przesle wyniki za 3 sekundy)')
        for i in range(3):
            print(' ' + str(i + 1))
            sleep(1)

        message = body.decode() + ' done'
        self.channel.basic_publish(exchange='technician_exchange',
                                   routing_key='resp_msg',
                                   body=message,
                                   properties=pika.BasicProperties(correlation_id=props.correlation_id))

        self.log_channel.basic_publish(exchange='log_exchange',
                                       routing_key='',
                                       body='Technik odsyla: ' + message)

        ch.basic_ack(delivery_tag=method.delivery_tag)
        print("Odeslalem odpowiedz")


if __name__ == "__main__":

    input_words = input("Uzycie: specjalizaja_1 specjalizacja_2\n").split()

    if len(input_words) == 2 \
            and input_words[0] in ('hip', 'knee', 'elbow') \
            and input_words[1] in ('hip', 'knee', 'elbow') \
            and input_words[0] != input_words[1]:
        Technician(input_words[0], input_words[1])
    else:
        print("Bledny input")
