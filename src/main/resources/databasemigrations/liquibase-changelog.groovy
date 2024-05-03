databaseChangeLog {

    changeSet(id: 'create folder table', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'folder')
            }
        }

        createTable(tableName: 'folder') {
            column(name: 'id', type: 'bigint(20)', autoIncrement: 'true') {
                primaryKey true
            }

            column(name: 'name', type: 'varchar(60)') {
                nullable false
            }

            column(name: 'description', type: 'varchar(250)')

            column(name: 'date_created', type: 'datetime', defaultValueComputed: 'CURRENT_TIMESTAMP') {
                nullable false
            }

            column(name: 'date_updated', type: 'datetime', defaultValueComputed: 'CURRENT_TIMESTAMP') {
                nullable false
            }

            column(name: 'status', type: 'varchar(50)') {
                nullable false
            }

            column(name: 'type', type: 'varchar(50)') {
                nullable false
            }

            column(name: 'author_id', type: 'bigint(20)') {
                nullable: false
            }
        }
    }

    changeSet(id: 'create asset table', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'asset')
            }
        }

        createTable(tableName: 'asset') {
            column(name: 'id', type: 'bigint(20)', autoIncrement: 'true') {
                primaryKey true
            }

            column(name: 'name', type: 'varchar(60)') {
                nullable false
            }

            column(name: 'description', type: 'varchar(250)')

            column(name: 'date_created', type: 'datetime', defaultValueComputed: 'CURRENT_TIMESTAMP') {
                nullable false
            }

            column(name: 'date_updated', type: 'datetime', defaultValueComputed: 'CURRENT_TIMESTAMP') {
                nullable false
            }

            column(name: 'status', type: 'varchar(50)') {
                nullable false
            }

            column(name: 'type', type: 'varchar(50)') {
                nullable false
            }

            column(name: 'author_id', type: 'bigint(20)') {
                nullable: false
            }

            column(name: 'folder_id', type: 'bigint(20)') {
                constraints(foreignKeyName: 'FK_ASSET_FOLDER', references: 'folder(id)')
            }
        }
    }

    changeSet(id: 'update asset folderId value to be mandatory', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'asset', columnName: 'folder_id')
        }

        sql('ALTER TABLE asset MODIFY folder_id BIGINT(20) NOT NULL')
    }

    changeSet(id: 'create asset_business table', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'asset_business')
            }
        }

        createTable(tableName: 'asset_business') {
            column(name: 'asset_id', type: 'bigint(20)') {
                constraints (nullable: false, foreignKeyName: 'FK_ASSET_BUSINESS_ASSET', references: 'asset(id)')
                primaryKey true
            }

            column(name: 'business_id', type: 'bigint(20)') {
                primaryKey true
            }
        }
    }

    changeSet(id: 'create asset_location table', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'asset_location')
            }
        }

        createTable(tableName: 'asset_location') {
            column(name: 'asset_id', type: 'bigint(20)') {
                constraints (nullable: false, foreignKeyName: 'FK_ASSET_LOCATION_ASSET', references: 'asset(id)')
                primaryKey true
            }

            column(name: 'location_id', type: 'bigint(20)') {
                primaryKey true
            }
        }
    }

    changeSet(id: 'add sales partner Id to asset table', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'asset', columnName: 'sales_partner_id')
            }
        }

        sql('ALTER TABLE asset ADD sales_partner_id BIGINT(20) NOT NULL')
    }

    changeSet(id: 'fix asset author Id value to be mandatory', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'asset', columnName: 'author_id')
        }

        sql('ALTER TABLE asset MODIFY author_id BIGINT(20) NOT NULL')
    }

    changeSet(id: 'fix folder author Id value to be mandatory', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'folder', columnName: 'author_id')
        }

        sql('ALTER TABLE folder MODIFY author_id BIGINT(20) NOT NULL')
    }

    changeSet(id: 'add template Id to asset table', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'asset', columnName: 'template_id')
            }
        }

        addColumn(tableName: 'asset') {
            column(name: 'template_id', type: 'bigint(20)', defaultValue: 0) {
                nullable false
            }
        }
    }

    changeSet(id: 'add start date to asset table', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'asset', columnName: 'start_date')
            }
        }

        addColumn(tableName: 'asset') {
            column(name: 'start_date', type: 'datetime', defaultValueComputed: 'CURRENT_TIMESTAMP') {
                nullable false
            }
        }
    }

    changeSet(id: 'add end date to asset table', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'asset', columnName: 'end_date')
            }
        }

        addColumn(tableName: 'asset') {
            column(name: 'end_date', type: 'datetime')
        }
    }

    changeSet(id: 'create distributed lock table', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'distributed_lock')
            }
        }

        createTable(tableName: 'distributed_lock') {
            column(name: 'id', type: 'bigint(20)', autoIncrement: 'true') {
                primaryKey true
            }

            column(name: 'name', type: 'varchar(255)') {
                constraints(unique: true, nullable: false)
            }

            column(name: 'until', type: 'datetime')
        }
    }

    changeSet(id: 'remove asset_business table', author: 'X') {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'asset_business')
        }

        dropTable(tableName: 'asset_business')
    }
}