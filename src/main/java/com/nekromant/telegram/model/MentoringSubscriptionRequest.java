package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;

@Entity
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class MentoringSubscriptionRequest extends ClientPaymentRequest {

}
