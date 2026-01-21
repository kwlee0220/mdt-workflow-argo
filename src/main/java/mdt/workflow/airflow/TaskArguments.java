package mdt.workflow.airflow;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TaskArguments {
	public static class ReferenceArgument extends TaskArgument {
		private final String m_refString;

		public ReferenceArgument(String argId, String refString) {
			super(argId);
			m_refString = refString;
		}

		public String getReference() {
			return m_refString;
		}

		@Override
		public String getPythonStatement() {
			return String.format("\"%s\": reference(\"%s\")", getId(), m_refString);
		}
	}
	
	public static class LiteralArgument extends TaskArgument {
		private final String m_literal;

		public LiteralArgument(String argId, String value) {
			super(argId);
			m_literal = value;
		}

		@Override
		public String getPythonStatement() {
			return String.format("\"%s\": literal(%s)", getId(), m_literal);
		}
	}
	
	public static class TaskOutputArgument extends TaskArgument {
		private final String m_taskId;
		private final String m_argumentName;

		public TaskOutputArgument(String argId, String taskId, String argument) {
			super(argId);
			
			m_taskId = taskId;
			m_argumentName = argument;
		}

		@Override
		public String getPythonStatement() {
			return String.format("\"%s\": task_output(\"%s\", \"%s\")", getId(), m_taskId, m_argumentName);
		}
	}
}
