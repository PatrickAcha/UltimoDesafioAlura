package com.desafio.forohub.domain.topico.repository;

import com.desafio.forohub.domain.topico.Estado;
import com.desafio.forohub.domain.topico.Topico;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
public class TopicoRepositoryTest {

    @Autowired
    private TopicoRepository topicoRepository;

    @Test
    public void testFindAllByEstadoIsNot() {
        Page<Topico> topicos = topicoRepository.findAllByEstadoIsNot(Estado.CERRADO, PageRequest.of(0, 10));
        assertThat(topicos).isNotEmpty();
    }
}