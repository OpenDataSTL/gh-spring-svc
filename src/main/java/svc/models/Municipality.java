
package svc.models;

import java.util.List;

import svc.types.HashableEntity;

public class Municipality {
	public HashableEntity<Municipality> id;
	public String name;
	public List<HashableEntity<Court>> courts;
	public String paymentUrl;
	public Boolean isSupported;
}