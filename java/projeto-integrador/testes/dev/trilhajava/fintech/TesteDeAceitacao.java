package dev.trilhajava.fintech;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * Sobe a aplicação inteira numa porta aleatória, como em produção, para testes "caixa-preta".
 *
 * As propriedades abaixo valem para qualquer marco: as de banco só têm efeito quando o projeto
 * tiver JPA (marco 2 em diante), as de administrador, quando tiver segurança (marco 4), e a de
 * eventos, a partir do marco 5: sem broker, os eventos ficam na outbox. Os testes do marco 5
 * ligam os eventos com @TestPropertySource, que tem precedência sobre as propriedades daqui.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = FintechApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:fintech-${random.uuid};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "fintech.admin.email=" + ApiDeTeste.ADMIN_EMAIL,
    "fintech.admin.senha=" + ApiDeTeste.ADMIN_SENHA,
    "fintech.jwt.segredo=" + ApiDeTeste.JWT_SEGREDO,
    "fintech.eventos.habilitados=false"
})
public @interface TesteDeAceitacao {
}
