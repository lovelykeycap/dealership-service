package dealership.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;

@Configuration
public class CommonJwtSecurityConfiguration {

    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> realmRolesGrantedAuthoritiesConverter() {
        return new RealmRolesGrantedAuthoritiesConverter();
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter(
            Converter<Jwt, Collection<GrantedAuthority>> realmRolesGrantedAuthoritiesConverter
    ) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(realmRolesGrantedAuthoritiesConverter);
        return converter;
    }
}
