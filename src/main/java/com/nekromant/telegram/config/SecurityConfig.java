package com.nekromant.telegram.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import java.util.Collection;
import java.util.Set;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${owner.userName}")
    private String ownerUserName;

    @Value("${owner.password}")
    private String ownerPassword;

    private UserDetailsService userDetailsService = new UserDetailsService() {

        @Override
        public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

            UserDetails userDetails = new UserDetails() {

                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {

                    GrantedAuthority role = new GrantedAuthority() {
                        private String title = "ROLE_admin";

                        @Override
                        public String getAuthority() {
                            return title;
                        }
                    };
                    return Set.of(role);
                }

                @Override
                public String getPassword() {
                    return getPasswordEncoder().encode(ownerPassword);
                }

                @Override
                public String getUsername() {
                    return ownerUserName;
                }

                @Override
                public boolean isAccountNonExpired() {
                    return true;
                }

                @Override
                public boolean isAccountNonLocked() {
                    return true;
                }

                @Override
                public boolean isCredentialsNonExpired() {
                    return true;
                }

                @Override
                public boolean isEnabled() {
                    return true;
                }
            };

            return userDetails;
        }
    };


    @Autowired
    private SuccessUserHandler successUserHandler;

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder authN) throws Exception {
        authN.userDetailsService(userDetailsService).passwordEncoder(getPasswordEncoder());
    }

    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors().and().csrf().disable()
                .authorizeRequests()
                .regexMatchers("/.*promocode\\?text=.*").permitAll()
                .antMatchers("/promocodepanel", "/promocode").hasRole("admin")
                .anyRequest().permitAll()
                .and().formLogin()
                .successHandler(successUserHandler)
                .and().logout();
    }


    @Bean
    public static PasswordEncoder getPasswordEncoder() {
        return new StandardPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider getDaoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(getPasswordEncoder());
        return authProvider;
    }
}
