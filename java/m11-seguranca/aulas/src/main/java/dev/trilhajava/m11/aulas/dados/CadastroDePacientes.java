package dev.trilhajava.m11.aulas.dados;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastroDePacientes {

    private static final Logger log = LoggerFactory.getLogger(CadastroDePacientes.class);

    private final PacienteRepository pacientes;
    private final IndiceCego indiceCego;

    public CadastroDePacientes(PacienteRepository pacientes, IndiceCego indiceCego) {
        this.pacientes = pacientes;
        this.indiceCego = indiceCego;
    }

    @Transactional
    public Paciente cadastrar(String nome, Cpf cpf) {
        String indice = indiceCego.doCpf(cpf, ConversorDeCpfCifrado.CONTEXTO);
        if (pacientes.findByCpfIndice(indice).isPresent()) {
            throw new IllegalStateException("já existe paciente com o CPF " + cpf);
        }
        Paciente salvo = pacientes.save(new Paciente(nome, cpf, indice));
        log.info("paciente cadastrado: id={} cpf={}", salvo.getId(), cpf);   // o toString do Cpf mascara
        return salvo;
    }

    @Transactional(readOnly = true)
    public Optional<Paciente> buscarPorCpf(Cpf cpf) {
        return pacientes.findByCpfIndice(indiceCego.doCpf(cpf, ConversorDeCpfCifrado.CONTEXTO));
    }
}
