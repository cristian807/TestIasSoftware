package com.ias_software.test.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageProcessingResult {

	private UsageRequestResponse request;

	/** true cuando la referencia ya habia sido procesada y se devuelve el resultado original. */
	private boolean duplicate;
}
