-- ---------------------------------------------------------------------------
-- Baza podataka za aplikaciju za zakazivanje termina u stomatoloskoj ordinaciji
--
-- Nazivi tabela i kolona odgovaraju vrednostima koje domenske klase vracaju
-- kroz metode vratiNazivTabele(), vratiKoloneZaUbacivanje() i vratiPrimarniKljuc().
--
-- Pokretanje:  mysql -u root -p < baza_stomatologija.sql
-- ---------------------------------------------------------------------------

DROP DATABASE IF EXISTS stomatologija;
CREATE DATABASE stomatologija
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE stomatologija;

-- ---------------------------------------------------------------------------
-- Tabele
-- ---------------------------------------------------------------------------

CREATE TABLE ordinacija (
    idOrdinacija INT NOT NULL AUTO_INCREMENT,
    naziv        VARCHAR(100) NOT NULL,
    adresa       VARCHAR(150) NOT NULL,
    PRIMARY KEY (idOrdinacija)
) ENGINE=InnoDB;

CREATE TABLE specijalizacija (
    idSpecijalizacija INT NOT NULL AUTO_INCREMENT,
    naziv             VARCHAR(100) NOT NULL,
    PRIMARY KEY (idSpecijalizacija),
    UNIQUE KEY uk_specijalizacija_naziv (naziv)
) ENGINE=InnoDB;

CREATE TABLE stomatolog (
    idStomatolog  INT NOT NULL AUTO_INCREMENT,
    ime           VARCHAR(50) NOT NULL,
    prezime       VARCHAR(50) NOT NULL,
    email         VARCHAR(100),
    korisnickoIme VARCHAR(50) NOT NULL,
    sifra         VARCHAR(50) NOT NULL,
    brojLicence   VARCHAR(30) NOT NULL,
    PRIMARY KEY (idStomatolog),
    UNIQUE KEY uk_stomatolog_korisnickoIme (korisnickoIme),
    UNIQUE KEY uk_stomatolog_brojLicence (brojLicence)
) ENGINE=InnoDB;

-- veza vise prema vise izmedju stomatologa i specijalizacije
CREATE TABLE stspec (
    idStomatolog      INT NOT NULL,
    idSpecijalizacija INT NOT NULL,
    datumSticanja     DATE NOT NULL,
    PRIMARY KEY (idStomatolog, idSpecijalizacija),
    CONSTRAINT fk_stspec_stomatolog FOREIGN KEY (idStomatolog)
        REFERENCES stomatolog (idStomatolog) ON DELETE CASCADE,
    CONSTRAINT fk_stspec_specijalizacija FOREIGN KEY (idSpecijalizacija)
        REFERENCES specijalizacija (idSpecijalizacija) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE usluga (
    idUsluga INT NOT NULL AUTO_INCREMENT,
    naziv    VARCHAR(100) NOT NULL,
    cena     DECIMAL(10,2) NOT NULL,
    trajanje INT NOT NULL COMMENT 'trajanje usluge u minutima',
    PRIMARY KEY (idUsluga)
) ENGINE=InnoDB;

CREATE TABLE pacijent (
    idPacijent   INT NOT NULL AUTO_INCREMENT,
    ime          VARCHAR(50) NOT NULL,
    prezime      VARCHAR(50) NOT NULL,
    brojTelefona VARCHAR(30),
    brojKnjizice VARCHAR(30) NOT NULL,
    idOrdinacija INT NOT NULL,
    PRIMARY KEY (idPacijent),
    UNIQUE KEY uk_pacijent_brojKnjizice (brojKnjizice),
    CONSTRAINT fk_pacijent_ordinacija FOREIGN KEY (idOrdinacija)
        REFERENCES ordinacija (idOrdinacija)
) ENGINE=InnoDB;

CREATE TABLE termin (
    idTermin     INT NOT NULL AUTO_INCREMENT,
    datum        DATE NOT NULL,
    vreme        TIME NOT NULL,
    status       ENUM('ZAKAZAN','OTKAZAN','ODRZAN') NOT NULL DEFAULT 'ZAKAZAN',
    napomena     VARCHAR(255),
    idStomatolog INT NOT NULL,
    idPacijent   INT NOT NULL,
    PRIMARY KEY (idTermin),
    -- zauzetost stomatologa se proverava u serverskom kontroleru, jer otkazan
    -- termin ne sme da blokira ponovno zakazivanje istog datuma i vremena
    KEY ix_termin_stomatolog_vreme (idStomatolog, datum, vreme),
    CONSTRAINT fk_termin_stomatolog FOREIGN KEY (idStomatolog)
        REFERENCES stomatolog (idStomatolog),
    CONSTRAINT fk_termin_pacijent FOREIGN KEY (idPacijent)
        REFERENCES pacijent (idPacijent)
) ENGINE=InnoDB;

-- stavka je slaba celina termina, pa se brise zajedno sa njim
CREATE TABLE stavkatermina (
    idTermin   INT NOT NULL,
    rb         INT NOT NULL,
    kolicina   INT NOT NULL DEFAULT 1,
    iznos      DECIMAL(10,2) NOT NULL,
    cenaUsluge DECIMAL(10,2) NOT NULL,
    idUsluga   INT NOT NULL,
    PRIMARY KEY (idTermin, rb),
    CONSTRAINT fk_stavkatermina_termin FOREIGN KEY (idTermin)
        REFERENCES termin (idTermin) ON DELETE CASCADE,
    CONSTRAINT fk_stavkatermina_usluga FOREIGN KEY (idUsluga)
        REFERENCES usluga (idUsluga)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Test podaci
-- ---------------------------------------------------------------------------

INSERT INTO ordinacija (naziv, adresa) VALUES
    ('Dental Centar', 'Bulevar kralja Aleksandra 73, Beograd'),
    ('Ordinacija Osmeh', 'Njegoseva 12, Novi Sad');

INSERT INTO specijalizacija (naziv) VALUES
    ('Ortodoncija'),
    ('Oralna hirurgija'),
    ('Protetika');

INSERT INTO stomatolog (ime, prezime, email, korisnickoIme, sifra, brojLicence) VALUES
    ('Petar', 'Petrovic', 'petar.petrovic@dental.rs', 'pera',  'pera123456',  'L-1001'),
    ('Milica', 'Jovanovic', 'milica.jovanovic@dental.rs', 'mica', 'mica123456', 'L-1002'),
    ('Nikola', 'Ilic', 'nikola.ilic@dental.rs', 'nikola', 'nikola123', 'L-1003');

INSERT INTO stspec (idStomatolog, idSpecijalizacija, datumSticanja) VALUES
    (1, 1, '2018-06-15'),
    (1, 3, '2021-09-01'),
    (2, 2, '2019-11-20'),
    (3, 3, '2022-02-10');

INSERT INTO usluga (naziv, cena, trajanje) VALUES
    ('Pregled', 1500.00, 20),
    ('Ciscenje kamenca', 4000.00, 45),
    ('Plomba', 3500.00, 40),
    ('Vadjenje zuba', 5000.00, 30),
    ('Izbeljivanje zuba', 12000.00, 60);

INSERT INTO pacijent (ime, prezime, brojTelefona, brojKnjizice, idOrdinacija) VALUES
    ('Marko', 'Markovic', '0641234567', '10000000001', 1),
    ('Jelena', 'Nikolic', '0637654321', '09876543212', 1),
    ('Stefan', 'Stankovic', '0611112222', '10000000003', 2),
    ('Ana', 'Popovic', '0653334444', '10000000004', 2);

INSERT INTO termin (datum, vreme, status, napomena, idStomatolog, idPacijent) VALUES
    ('2026-09-01', '09:00:00', 'ZAKAZAN', 'Redovna kontrola', 1, 1),
    ('2026-09-01', '10:00:00', 'ZAKAZAN', 'Bol u gornjem levom kutnjaku', 1, 2),
    ('2026-09-02', '12:30:00', 'ODRZAN',  '', 2, 3),
    ('2026-09-03', '08:15:00', 'OTKAZAN', 'Pacijent otkazao dan ranije', 1, 4);

INSERT INTO stavkatermina (idTermin, rb, kolicina, iznos, cenaUsluge, idUsluga) VALUES
    (1, 1, 1,  1500.00, 1500.00, 1),
    (2, 1, 1,  3500.00, 3500.00, 3),
    (2, 2, 1,  1500.00, 1500.00, 1),
    (3, 1, 1,  4000.00, 4000.00, 2),
    (4, 1, 1, 12000.00, 12000.00, 5);
