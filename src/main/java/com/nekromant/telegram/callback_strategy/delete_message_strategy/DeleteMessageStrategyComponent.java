package com.nekromant.telegram.callback_strategy.delete_message_strategy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeleteMessageStrategyComponent {
    private DeleteMessageStrategy deleteMessageStrategy;
}
