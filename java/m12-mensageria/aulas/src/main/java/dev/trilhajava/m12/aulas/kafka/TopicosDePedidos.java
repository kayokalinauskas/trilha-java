package dev.trilhajava.m12.aulas.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
class TopicosDePedidos {

    static final String EVENTOS = "pedidos.eventos";

    /**
     * O KafkaAdmin do Spring cria o tópico na subida, se ele não existir (e não altera um que já existe).
     * 3 partições = até 3 consumidores trabalhando em paralelo no mesmo grupo.
     * Réplicas = 1 só porque o broker de desenvolvimento é um só; em produção, 3
     * (com min.insync.replicas=2, assunto da aula 12.08).
     */
    @Bean
    NewTopic eventosDePedidos() {
        return TopicBuilder.name(EVENTOS).partitions(3).replicas(1).build();
    }
}
