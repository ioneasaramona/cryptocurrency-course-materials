import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class Transaction {
    // clasa Input contine inputurile pentru o tranzactie
    public class Input {
        /** hash of the Transaction whose output is being used */
        public byte[] prevTxHash; //contine Hashul unui input folosit
        /** used output's index in the previous transaction */
        public int outputIndex; //contine indexul unui input folosit
        /** the signature produced to check validity */
        public byte[] signature; //contine semnatura unui input pentru a verifica daca este valid
        // metoda pentru a crea un input, ca parametru este hashul unei tranzactii si indexul
        public Input(byte[] prevHash, int index) {
            if (prevHash == null)
                prevTxHash = null;
            else
                prevTxHash = Arrays.copyOf(prevHash, prevHash.length); //copie hashul in prevTxHash
            outputIndex = index; //copie indexul in outputIndex
        }
        //copie parametrul sig in membrul signature
        public void addSignature(byte[] sig) {
            if (sig == null)
                signature = null;
            else
                signature = Arrays.copyOf(sig, sig.length);
        }
    }
    // clasa Output contine outputurile pentru o tranzactie
    public class Output {
        /** value in bitcoins of the output */
        public double value; // valoarea tranzactiei
        /** the address or public key of the recipient */
        public PublicKey address; // adresa recipientului
        // constructorul unui output primeste valoarea si adresa
        public Output(double v, PublicKey addr) {
            value = v;
            address = addr;
        }
    }

    /** hash of the transaction, its unique id */
    private byte[] hash;
    private ArrayList<Input> inputs; //lista cu toate inputurile
    private ArrayList<Output> outputs; //lista cu toate outputurile
    // constructor initial
    public Transaction() {
        inputs = new ArrayList<Input>(); //se creeaza o lista inputs goala
        outputs = new ArrayList<Output>(); //se creeaza o lista outputs goala
    }
    // constructor care copie o tranzactie
    public Transaction(Transaction tx) {
        hash = tx.hash.clone(); //copie hashul
        inputs = new ArrayList<Input>(tx.inputs); //copie lista de inputuri
        outputs = new ArrayList<Output>(tx.outputs); //copie lista de outputuri
    }
    // adauga un Input la tranzactie, foloseste constructorul de la Input
    public void addInput(byte[] prevTxHash, int outputIndex) {
        Input in = new Input(prevTxHash, outputIndex);
        inputs.add(in);
    }
    // adauga un Output la tranzactie, foloseste constructorul de la Output
    public void addOutput(double value, PublicKey address) {
        Output op = new Output(value, address);
        outputs.add(op);
    }
    // sterge Inputul cu nr index din lista de inputuri
    public void removeInput(int index) {
        inputs.remove(index);
    }
    // sterge Inputul ut din Tranzactie
    public void removeInput(UTXO ut) {
        for (int i = 0; i < inputs.size(); i++) {
            Input in = inputs.get(i);
            UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
            if (u.equals(ut)) {
                inputs.remove(i);
                return;
            }
        }
    }

    public byte[] getRawDataToSign(int index) {
        // ith input and all outputs
        ArrayList<Byte> sigData = new ArrayList<Byte>();
        if (index > inputs.size())
            return null;
        Input in = inputs.get(index);
        byte[] prevTxHash = in.prevTxHash;
        ByteBuffer b = ByteBuffer.allocate(Integer.SIZE / 8);
        b.putInt(in.outputIndex);
        byte[] outputIndex = b.array();
        if (prevTxHash != null)
            for (int i = 0; i < prevTxHash.length; i++)
                sigData.add(prevTxHash[i]);
        for (int i = 0; i < outputIndex.length; i++)
            sigData.add(outputIndex[i]);
        for (Output op : outputs) {
            ByteBuffer bo = ByteBuffer.allocate(Double.SIZE / 8);
            bo.putDouble(op.value);
            byte[] value = bo.array();
            byte[] addressBytes = op.address.getEncoded();
            for (int i = 0; i < value.length; i++)
                sigData.add(value[i]);

            for (int i = 0; i < addressBytes.length; i++)
                sigData.add(addressBytes[i]);
        }
        byte[] sigD = new byte[sigData.size()];
        int i = 0;
        for (Byte sb : sigData)
            sigD[i++] = sb;
        return sigD;
    }
    //adauga semnatura la inputul cu nr index
    public void addSignature(byte[] signature, int index) {
        inputs.get(index).addSignature(signature);
    }
    // calculeaza valoarea Raw a unei tranzactii, va fi folosita pentru a calcula hashul unei tranzactii
    public byte[] getRawTx() {
        ArrayList<Byte> rawTx = new ArrayList<Byte>(); //creeaza o lista de Bytes
        for (Input in : inputs) { // iau toate inputurile
            byte[] prevTxHash = in.prevTxHash; // iau Hashul fiecarui input
            ByteBuffer b = ByteBuffer.allocate(Integer.SIZE / 8);
            b.putInt(in.outputIndex); // pune indexul intr-un buffer
            byte[] outputIndex = b.array(); //?
            byte[] signature = in.signature; //ia semnatura
            if (prevTxHash != null) //daca nu e prima tranzactie
                for (int i = 0; i < prevTxHash.length; i++)
                    rawTx.add(prevTxHash[i]); // pune hashul in rawTx
            for (int i = 0; i < outputIndex.length; i++)
                rawTx.add(outputIndex[i]); // adauga indexul in rawTx
            if (signature != null) // daca are semnatura
                for (int i = 0; i < signature.length; i++)
                    rawTx.add(signature[i]); // adauga semantura in rawTx
        }
        for (Output op : outputs) { // ia toate outputurile
            ByteBuffer b = ByteBuffer.allocate(Double.SIZE / 8); // creeaza un buffer
            b.putDouble(op.value); // pune valoarea in buffer
            byte[] value = b.array(); //?
            byte[] addressBytes = op.address.getEncoded(); //ia adresa in addressBytes
            for (int i = 0; i < value.length; i++) {
                rawTx.add(value[i]); //pune toate valorile in lista
            }
            for (int i = 0; i < addressBytes.length; i++) {
                rawTx.add(addressBytes[i]); //pune toate adresele in lista
            }

        }
        byte[] tx = new byte[rawTx.size()]; //tx e dimensiunea in bytes
        int i = 0;
        for (Byte b : rawTx)
            tx[i++] = b; //copie din b in tx
        return tx;
    }
    // calculeaza hashul unei tranzactii
    public void finalize() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(getRawTx());
            hash = md.digest();
        } catch (NoSuchAlgorithmException x) {
            x.printStackTrace(System.err);
        }
    }
    // seteaza direct hashul cu o valoare
    public void setHash(byte[] h) {
        hash = h;
    }
    // citeste hashul unei tranzactii
    public byte[] getHash() {
        return hash;
    }
    // returneaza lista de inputs pentru o tranzactie
    public ArrayList<Input> getInputs() {
        return inputs;
    }
    // returneaza lista de outputs pentru o tranzactie
    public ArrayList<Output> getOutputs() {
        return outputs;
    }
    // returneaza Input cu nr index pentru o tranzactie
    public Input getInput(int index) {
        if (index < inputs.size()) {
            return inputs.get(index);
        }
        return null;
    }
    // returneaza Output cu nr index pentur o tranzactie
    public Output getOutput(int index) {
        if (index < outputs.size()) {
            return outputs.get(index);
        }
        return null;
    }
    // returneaza nr de Inputuri
    public int numInputs() {
        return inputs.size();
    }
    // returneaza nr de Outputuri
    public int numOutputs() {
        return outputs.size();
    }
}
