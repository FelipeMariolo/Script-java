import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/?user=root";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

public class cadastroGUI extends JFrame {
    private JTable tabelaClientes;
    private JTable tabelaServicos;
    private DefaultTableModel modelClientes;
    private DefaultTableModel modelServicos;

    public cadastroGUI() {
        super("Cadastro para os clientes e integração de servico");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        modelClientes = new DefaultTableModel(new Object[]{"ID", "Nome", "CPF", "CNPJ"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        modelServicos = new DefaultTableModel(new Object[]{"ID", "Descrição", "Data", "Valor", "Cliente ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        initComponents();
        carregarDados();
    }

    private void initComponents() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabelaClientes = new JTable(modelClientes);
        tabelaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollClientes = new JScrollPane(tabelaClientes);
        scrollClientes.setBorder(BorderFactory.createTitledBorder("Clientes"));
        
        JPanel painelBotoesClientes = criarPainelBotoesClientes();

        tabelaServicos = new JTable(modelServicos);
        tabelaServicos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollServicos = new JScrollPane(tabelaServicos);
        scrollServicos.setBorder(BorderFactory.createTitledBorder("Serviços"));
        
        JPanel painelBotoesServicos = criarPainelBotoesServicos();

        JPanel painelClientes = new JPanel(new BorderLayout());
        painelClientes.add(scrollClientes, BorderLayout.CENTER);
        painelClientes.add(painelBotoesClientes, BorderLayout.SOUTH);
        
        JPanel painelServicos = new JPanel(new BorderLayout());
        painelServicos.add(scrollServicos, BorderLayout.CENTER);
        painelServicos.add(painelBotoesServicos, BorderLayout.SOUTH);

        JPanel painelTabelas = new JPanel(new GridLayout(1, 2, 10, 10));
        painelTabelas.add(painelClientes);
        painelTabelas.add(painelServicos);

        JButton btnAtualizar = new JButton("Atualizar Todos os Dados");
        btnAtualizar.addActionListener(e -> carregarDados());
        
        JPanel painelAtualizar = new JPanel();
        painelAtualizar.add(btnAtualizar);

        painelPrincipal.add(painelTabelas, BorderLayout.CENTER);
        painelPrincipal.add(painelAtualizar, BorderLayout.SOUTH);

        add(painelPrincipal);
    }

    private JPanel criarPainelBotoesClientes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton btnAdicionar = new JButton("Adicionar");
        btnAdicionar.addActionListener(this::adicionarCliente);
        
        JButton btnEditar = new JButton("Editar");
        btnEditar.addActionListener(this::editarCliente);
        
        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.addActionListener(this::excluirCliente);
        
        painel.add(btnAdicionar);
        painel.add(btnEditar);
        painel.add(btnExcluir);
        
        return painel;
    }

    private JPanel criarPainelBotoesServicos() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton btnAdicionar = new JButton("Adicionar");
        btnAdicionar.addActionListener(this::adicionarServico);
        
        JButton btnEditar = new JButton("Editar");
        btnEditar.addActionListener(this::editarServico);
        
        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.addActionListener(this::excluirServico);
        
        painel.add(btnAdicionar);
        painel.add(btnEditar);
        painel.add(btnExcluir);
        
        return painel;
    }

    private void carregarDados() {
        carregarClientes();
        carregarServicos();
    }

    private void carregarClientes() {
        modelClientes.setRowCount(0);
        String sql = "SELECT id, nome_cliente, cpf, cnpj FROM clientes ORDER BY id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                modelClientes.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nome_cliente"),
                    formatarCPF(rs.getString("cpf")),
                    formatarCNPJ(rs.getString("cnpj"))
                });
            }
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar clientes", e);
        }
    }

    private void carregarServicos() {
        modelServicos.setRowCount(0);
        String sql = "SELECT id, descricao, data_servico, valor, cliente_id FROM servicos ORDER BY id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                modelServicos.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("descricao"),
                    formatarData(rs.getDate("data_servico")),
                    formatarValor(rs.getBigDecimal("valor")),
                    rs.getInt("cliente_id")
                });
            }
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar serviços", e);
        }
    }

    private void adicionarCliente(ActionEvent e) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField txtNome = new JTextField();
        JTextField txtCpf = new JTextField();
        JTextField txtCnpj = new JTextField();
        
        panel.add(new JLabel("Nome:"));
        panel.add(txtNome);
        panel.add(new JLabel("CPF (apenas números):"));
        panel.add(txtCpf);
        panel.add(new JLabel("CNPJ (apenas números):"));
        panel.add(txtCnpj);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Adicionar Novo Cliente", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Obter próximo ID
                int nextId = 1;
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM clientes")) {
                    if (rs.next()) {
                        nextId = rs.getInt(1) + 1;
                    }
                }
                
                // Inserir novo cliente
                String sql = "INSERT INTO clientes (id, nome_cliente, cpf, cnpj) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, nextId);
                    pstmt.setString(2, txtNome.getText().trim());
                    
                    // Formata CPF/CNPJ (remove formatação existente)
                    String cpf = txtCpf.getText().replaceAll("[^0-9]", "");
                    String cnpj = txtCnpj.getText().replaceAll("[^0-9]", "");
                    
                    pstmt.setString(3, cpf.isEmpty() ? null : cpf);
                    pstmt.setString(4, cnpj.isEmpty() ? null : cnpj);
                    
                    pstmt.executeUpdate();
                    carregarDados();
                    JOptionPane.showMessageDialog(this, "Cliente adicionado com sucesso!");
                }
            } catch (SQLException ex) {
                mostrarErro("Erro ao adicionar cliente", ex);
            }
        }
    }

    private void editarCliente(ActionEvent e) {
        int selectedRow = tabelaClientes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selecione um cliente para editar",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modelClientes.getValueAt(selectedRow, 0);
        String nome = (String) modelClientes.getValueAt(selectedRow, 1);
        String cpf = ((String) modelClientes.getValueAt(selectedRow, 2)).replaceAll("[^0-9]", "");
        String cnpj = ((String) modelClientes.getValueAt(selectedRow, 3)).replaceAll("[^0-9]", "");
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField txtNome = new JTextField(nome);
        JTextField txtCpf = new JTextField(cpf);
        JTextField txtCnpj = new JTextField(cnpj);
        
        panel.add(new JLabel("Nome:"));
        panel.add(txtNome);
        panel.add(new JLabel("CPF:"));
        panel.add(txtCpf);
        panel.add(new JLabel("CNPJ:"));
        panel.add(txtCnpj);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Editar Cliente", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE clientes SET nome_cliente = ?, cpf = ?, cnpj = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, txtNome.getText().trim());
                    pstmt.setString(2, txtCpf.getText().isEmpty() ? null : txtCpf.getText().replaceAll("[^0-9]", ""));
                    pstmt.setString(3, txtCnpj.getText().isEmpty() ? null : txtCnpj.getText().replaceAll("[^0-9]", ""));
                    pstmt.setInt(4, id);
                    
                    int updated = pstmt.executeUpdate();
                    if (updated > 0) {
                        carregarDados();
                        JOptionPane.showMessageDialog(this, "Cliente atualizado com sucesso!");
                    }
                }
            } catch (SQLException ex) {
                mostrarErro("Erro ao atualizar cliente", ex);
            }
        }
    }

    private void excluirCliente(ActionEvent e) {
        int selectedRow = tabelaClientes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selecione um cliente para excluir",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modelClientes.getValueAt(selectedRow, 0);
        String nome = (String) modelClientes.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Tem certeza que deseja excluir o cliente:\n" + nome + " (ID: " + id + ")?\n" +
            "Esta ação também excluirá todos os serviços associados.", 
            "Confirmar Exclusão", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                
                try {
                    // Primeiro exclui os serviços associados
                    try (PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM servicos WHERE cliente_id = ?")) {
                        pstmt.setInt(1, id);
                        pstmt.executeUpdate();
                    }
                    
                    // Depois exclui o cliente
                    try (PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM clientes WHERE id = ?")) {
                        pstmt.setInt(1, id);
                        int deleted = pstmt.executeUpdate();
                        
                        if (deleted > 0) {
                            conn.commit();
                            carregarDados();
                            JOptionPane.showMessageDialog(this, 
                                "Cliente e serviços associados excluídos com sucesso!");
                        }
                    }
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                mostrarErro("Erro ao excluir cliente", ex);
            }
        }
    }

    private void adicionarServico(ActionEvent e) {
        if (modelClientes.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Não há clientes cadastrados. Cadastre um cliente primeiro.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        
        // Combo com clientes disponíveis
        JComboBox<String> comboClientes = new JComboBox<>();
        for (int i = 0; i < modelClientes.getRowCount(); i++) {
            int id = (int) modelClientes.getValueAt(i, 0);
            String nome = (String) modelClientes.getValueAt(i, 1);
            comboClientes.addItem(id + " - " + nome);
        }
        
        JTextField txtDescricao = new JTextField();
        JTextField txtData = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        JTextField txtValor = new JTextField("0.00");
        
        panel.add(new JLabel("Cliente:"));
        panel.add(comboClientes);
        panel.add(new JLabel("Descrição:"));
        panel.add(txtDescricao);
        panel.add(new JLabel("Data (AAAA-MM-DD):"));
        panel.add(txtData);
        panel.add(new JLabel("Valor:"));
        panel.add(txtValor);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Adicionar Novo Serviço", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                // Extrai o ID do cliente selecionado
                int clienteId = Integer.parseInt(
                    comboClientes.getSelectedItem().toString().split(" - ")[0]);
                
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Obter próximo ID
                    int nextId = 1;
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM servicos")) {
                        if (rs.next()) {
                            nextId = rs.getInt(1) + 1;
                        }
                    }
                    
                    String sql = "INSERT INTO servicos (id, descricao, data_servico, valor, cliente_id) " +
                                 "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, nextId);
                        pstmt.setString(2, txtDescricao.getText().trim());
                        pstmt.setDate(3, Date.valueOf(txtData.getText()));
                        pstmt.setBigDecimal(4, new java.math.BigDecimal(txtValor.getText()));
                        pstmt.setInt(5, clienteId);
                        
                        pstmt.executeUpdate();
                        carregarDados();
                        JOptionPane.showMessageDialog(this, "Serviço adicionado com sucesso!");
                    }
                }
            } catch (Exception ex) {
                mostrarErro("Erro ao adicionar serviço", ex);
            }
        }
    }

    private void editarServico(ActionEvent e) {
        int selectedRow = tabelaServicos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selecione um serviço para editar",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modelServicos.getValueAt(selectedRow, 0);
        String descricao = (String) modelServicos.getValueAt(selectedRow, 1);
        String data = ((String) modelServicos.getValueAt(selectedRow, 2)).replaceAll("/", "-");
        String valor = ((String) modelServicos.getValueAt(selectedRow, 3)).replaceAll("[^0-9.]", "");
        int clienteId = (int) modelServicos.getValueAt(selectedRow, 4);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        
        JComboBox<String> comboClientes = new JComboBox<>();
        for (int i = 0; i < modelClientes.getRowCount(); i++) {
            int cId = (int) modelClientes.getValueAt(i, 0);
            String nome = (String) modelClientes.getValueAt(i, 1);
            comboClientes.addItem(cId + " - " + nome);
            if (cId == clienteId) {
                comboClientes.setSelectedIndex(i);
            }
        }
        
        JTextField txtDescricao = new JTextField(descricao);
        JTextField txtData = new JTextField(data);
        JTextField txtValor = new JTextField(valor);
        
        panel.add(new JLabel("Cliente:"));
        panel.add(comboClientes);
        panel.add(new JLabel("Descrição:"));
        panel.add(txtDescricao);
        panel.add(new JLabel("Data (AAAA-MM-DD):"));
        panel.add(txtData);
        panel.add(new JLabel("Valor:"));
        panel.add(txtValor);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Editar Serviço", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE servicos SET descricao = ?, data_servico = ?, valor = ?, cliente_id = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, txtDescricao.getText().trim());
                    pstmt.setDate(2, Date.valueOf(txtData.getText()));
                    pstmt.setBigDecimal(3, new java.math.BigDecimal(txtValor.getText()));
                    pstmt.setInt(4, Integer.parseInt(comboClientes.getSelectedItem().toString().split(" - ")[0]));
                    pstmt.setInt(5, id);
                    
                    int updated = pstmt.executeUpdate();
                    if (updated > 0) {
                        carregarDados();
                        JOptionPane.showMessageDialog(this, "Serviço atualizado com sucesso!");
                    }
                }
            } catch (Exception ex) {
                mostrarErro("Erro ao editar serviço", ex);
            }
        }
    }

    private void excluirServico(ActionEvent e) {
        int selectedRow = tabelaServicos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selecione um serviço para excluir",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modelServicos.getValueAt(selectedRow, 0);
        String descricao = (String) modelServicos.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Tem certeza que deseja excluir o serviço:\n\"" + descricao + "\"?", 
            "Confirmar Exclusão", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM servicos WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id);
                    int deleted = pstmt.executeUpdate();
                    
                    if (deleted > 0) {
                        carregarDados();
                        JOptionPane.showMessageDialog(this, "Serviço excluído com sucesso!");
                    }
                }
            } catch (SQLException ex) {
                mostrarErro("Erro ao excluir serviço", ex);
            }
        }
    }

    private String formatarCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    private String formatarCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) return cnpj;
        return cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    private String formatarData(Date data) {
        return new SimpleDateFormat("dd/MM/yyyy").format(data);
    }

    private String formatarValor(java.math.BigDecimal valor) {
        return String.format("R$ %.2f", valor);
    }

    private void mostrarErro(String mensagem, Exception e) {
        JOptionPane.showMessageDialog(this, 
            mensagem + ": " + e.getMessage(), 
            "Erro", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new GestaoClientesServicosCompleta().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Erro ao iniciar a aplicação: " + e.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}