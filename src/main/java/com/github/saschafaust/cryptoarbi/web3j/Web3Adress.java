package com.github.saschafaust.cryptoarbi.web3j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Wrapper for a adress-string
 */
public class Web3Adress {

    private final String adress;

    public static Web3Adress from(String adress) {
        return new Web3Adress(adress);
    }

    private Web3Adress(String adress) {
        if (StringUtils.isBlank(adress)) {
            throw new IllegalArgumentException("adress can't be empty");
        }
        this.adress = adress;
    }

    public String get() {
        return this.adress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass().equals(this.getClass())) {
            Web3Adress other = (Web3Adress) obj;
            return new EqualsBuilder().append(this.adress.toLowerCase(), other.adress.toLowerCase()).isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.adress.toLowerCase()).toHashCode();
    }

}
