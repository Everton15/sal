
package jcu.sal.comms;

import jcu.sal.xml.XMLHelper;
import jcu.sal.xml.MessageContent;
import jcu.sal.xml.MessageDescription;
import jcu.sal.xml.Argument;
import jcu.sal.xml.ArgumentDescription;
import jcu.sal.xml.ValidType;

import java.util.ArrayList;

import javax.xml.bind.UnmarshalException;

import org.apache.commons.codec.binary.Base64;

public class MessageValidator {

	public static String reasonInvalid(MessageContent content, MessageDescription description) {
		if (content == null) {
			return "The content is null.";
		}

		try {
			XMLHelper.validate(content);
		} catch(UnmarshalException ue) {
			return "The content failed schema validation with this message - " + ue.getMessage();
		} catch(Exception e) {
			return e.getMessage();
		}

		if (description == null) {
			return null;
		}

		String reasons = "";

		if (content.getArgument().size() != description.getArgument().size()) {
			reasons += "Incorrect number of arguments -";
			reasons += " Expected: " + description.getArgument().size();
			reasons += " Found: " + content.getArgument().size();
			reasons += ".\n";
		}

		int size = content.getArgument().size();

		if (size > description.getArgument().size()) {
			size = description.getArgument().size();
		}

		for (int i = 0; i < size; ++i) {
			String argReason = reasonInvalid(content.getArgument().get(i), description.getArgument().get(i));
			if (argReason != null) {
				reasons += "Argument " + description.getArgument().get(i).getName() + " invalid - ";
				reasons += argReason;
			}
		}

		if (reasons.length() == 0) {
			return null;
		} else {
			return reasons;
		}
	}

	public static String reasonInvalid(Argument argument, ArgumentDescription description) {
		int size = argument.getValue().size();

		if (description.isArray()) {
			ArrayList<Integer> badTypeIndices = new ArrayList<Integer>();

			for (int i = 0; i < size; ++i) {
				if (!valid(argument.getValue().get(i), description.getType())) {
					badTypeIndices.add(i);
				}
			}

			if (badTypeIndices.size() == 0) {
				return null;
			} else {
				if (badTypeIndices.size() == 1) {
					String reason = "Value at index ";
					reason += String.valueOf(badTypeIndices.get(0));
					reason += " is not of type ";
					reason += description.getType().value();
					reason += ".\n";
					return reason;
				} else {
					String reason = "Values at indices ";
					for (int i = 0; i < badTypeIndices.size(); ++i) {
						reason += String.valueOf(badTypeIndices.get(0));
						if (i < size - 2) {
							reason += ", ";
						} else if (i == size - 2) {
							reason += " and ";
						}
					}
					reason += " are not of type ";
					reason += description.getType().value();
					reason += ".\n";
					return reason;
				}
			}

		} else {
			if (size == 0) {
				return null;
			}

			if (size > 1) {
				return "More than one value for non-array argument.\n";
			}

			if (valid(argument.getValue().get(0), description.getType())) {
				return null;
			} else {
				return "Value is not of type " + description.getType().value() + ".\n";
			}
		}
	}


	public static boolean valid(MessageContent content, MessageDescription description) {
		return (reasonInvalid(content, description) == null);
	}

	public static boolean valid(Argument argument, ArgumentDescription description) {
		return (reasonInvalid(argument, description) == null);
	}

	public static boolean valid(String value, ValidType type) {
		switch (type) {
			case INT:
				return validInt(value);
			case FLOAT:
				return validFloat(value);
			case BOOLEAN:
				return validBoolean(value);
			case STRING:
				return validString(value);
			case BINARY:
				return validBinary(value);
			default:
				return false;
		}
	}

	public static boolean validInt(String value) {
		try {
			Integer i = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static boolean validFloat(String value) {
		try {
			Float f = Float.parseFloat(value);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static boolean validBoolean(String value) {
		if (value == null) {
			return false;
		}

		String lower = value.toLowerCase();
		if (lower.equals("true") || lower.equals("false")) {
			return true;
		}

		return false;
	}

	public static boolean validString(String value) {
		return true;
	}

	public static boolean validBinary(String value) {
		try {
			return Base64.isArrayByteBase64(value.getBytes("UTF-8"));
		} catch (Exception e) {
			return false;
		}
	}
}
